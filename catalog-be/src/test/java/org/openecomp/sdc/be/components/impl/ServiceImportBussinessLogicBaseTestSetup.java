/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.components.validation.component.*;
import org.openecomp.sdc.be.components.validation.service.*;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.facade.operations.CatalogOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ServiceImportBussinessLogicBaseTestSetup extends BaseBusinessLogicMock {
    protected ServiceImportBusinessLogic sIB1;
    protected static final String SERVICE_CATEGORY = "Mobility";
    protected static final String INSTANTIATION_TYPE = "A-la-carte";
    protected final ServletContext servletContext = Mockito.mock(ServletContext.class);
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
    IElementOperation mockElementDao = new ElementOperationMock();
    DistributionEngine distributionEngine = Mockito.mock(DistributionEngine.class);
    ServiceDistributionValidation serviceDistributionValidation = Mockito.mock(ServiceDistributionValidation.class);
    ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    ForwardingPathValidator forwardingPathValidator = Mockito.mock(ForwardingPathValidator.class);
    UiComponentDataConverter uiComponentDataConverter = Mockito.mock(UiComponentDataConverter.class);
    NodeFilterOperation serviceFilterOperation = Mockito.mock(NodeFilterOperation.class);
    NodeFilterValidator serviceFilterValidator = Mockito.mock(NodeFilterValidator.class);
    protected ServiceTypeValidator serviceTypeValidator = new ServiceTypeValidator(componentsUtils);
    protected ServiceCategoryValidator serviceCategoryValidator = new ServiceCategoryValidator(componentsUtils, mockElementDao);
    protected ServiceRoleValidator serviceRoleValidator = new ServiceRoleValidator(componentsUtils);
    protected ServiceFunctionValidator serviceFunctionValidator = new ServiceFunctionValidator(componentsUtils);
    protected ServiceInstantiationTypeValidator serviceInstantiationTypeValidator = new ServiceInstantiationTypeValidator(componentsUtils);
    protected ComponentDescriptionValidator componentDescriptionValidator = new ComponentDescriptionValidator(componentsUtils);
    protected ComponentProjectCodeValidator componentProjectCodeValidator = new ComponentProjectCodeValidator(componentsUtils);
    protected ComponentIconValidator componentIconValidator = new ComponentIconValidator(componentsUtils);
    protected ComponentContactIdValidator componentContactIdValidator = new ComponentContactIdValidator(componentsUtils);
    protected ComponentTagsValidator componentTagsValidator = new ComponentTagsValidator(componentsUtils);
    protected ComponentNameValidator componentNameValidator = new ComponentNameValidator(componentsUtils, toscaOperationFacade);
    protected final ComponentValidator componentValidator = Mockito.mock(ComponentValidator.class);
    protected ServiceValidator serviceValidator = createServiceValidator();

    protected User user = null;
    protected Resource genericService = null;

    private static final String RESOURCE_NAME = "My-Resource_Name with   space";
    private static final String RESOURCE_TOSCA_NAME = "My-Resource_Tosca_Name";
    private static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    private static final String RESOURCE_SUBCATEGORY = "Router";
    protected static final String CERTIFIED_VERSION = "1.0";
    protected static final String UNCERTIFIED_VERSION = "0.2";
    protected static final String COMPONNET_ID = "myUniqueId";
    protected static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";

    protected static final String SERVICE_ROLE = JsonPresentationFields.SERVICE_ROLE.getPresentation();
    protected static final String SERVICE_TYPE = JsonPresentationFields.SERVICE_TYPE.getPresentation();
    protected static final String SERVICE_FUNCTION = JsonPresentationFields.SERVICE_FUNCTION.getPresentation();

    public ServiceImportBussinessLogicBaseTestSetup() {

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

    @Before
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
        when(userValidations.validateUserExists(eq("jh0003"))).thenReturn(user);
        when(userValidations.validateUserNotEmpty(eq(user), anyString())).thenReturn(user);
//        when(userValidations.validateUserRole(user))
        // Servlet Context attributes
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
//        when(servletContext.getAttribute(Constants.SERVICE_OPERATION_MANAGER)).thenReturn(new ServiceOperation());
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
        when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);
        when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
        when(catalogOperation.updateCatalog(Mockito.any(), Mockito.any())).thenReturn(ActionStatus.OK);
        // artifact bussinesslogic
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        when(artifactBl.createArtifactPlaceHolderInfo(Mockito.any(), Mockito.anyString(), Mockito.anyMap(), Mockito.any(User.class), Mockito.any(ArtifactGroupTypeEnum.class))).thenReturn(artifactDef);

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


        sIB1 = new ServiceImportBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
                groupTypeOperation, groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation,
                artifactBl, distributionEngine, componentInstanceBusinessLogic,
                serviceDistributionValidation, forwardingPathValidator, uiComponentDataConverter, serviceFilterOperation,
                serviceFilterValidator, artifactToscaOperation, componentContactIdValidator,
                componentNameValidator, componentTagsValidator, componentValidator,
                componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator);


        mockAuditingDaoLogic();

        responseManager = ResponseFormatManager.getInstance();
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
        // service.setVendorName("Motorola");
        // service.setVendorRelease("1.0.0");
        service.setIcon("defaulticon");
        // service.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
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

    protected Resource createResourceObject(boolean afterCreate) {
        Resource resource = new Resource();
        resource.setUniqueId("sid");
        resource.setName("Service");
        CategoryDefinition category = new CategoryDefinition();
        category.setName(SERVICE_CATEGORY);
        category.setIcons(Collections.singletonList("defaulticon"));
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        resource.setCategories(categories);


        resource.setDescription("description");
        List<String> tgs = new ArrayList<>();
        tgs.add(resource.getName());
        resource.setTags(tgs);
        resource.setIcon("defaulticon");
        resource.setContactId("aa1234");
        resource.setProjectCode("12345");


        if (afterCreate) {
            resource.setVersion("0.1");
            resource.setUniqueId(resource.getName() + ":" + resource.getVersion());
            resource.setCreatorUserId(user.getUserId());
            resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        }
        return resource;
    }
	
protected Resource createParseResourceObject(boolean afterCreate) {
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
        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList= new ArrayList<>();
        requirements.put("test", requirementDefinitionList);
        resource.setRequirements(requirements);

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
    protected Resource setupGenericServiceMock() {
        Resource genericService = new Resource();
        genericService.setVersion("1.0");
        genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
        return genericService;
    }

    protected UploadComponentInstanceInfo createUploadComponentInstanceInfo(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("UploadComponentInstanceInfo");
        return uploadComponentInstanceInfo;
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

        List<ResourceAdminEvent> list = new ArrayList<ResourceAdminEvent>() {
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

    protected void assertComponentException(ComponentException e, ActionStatus expectedStatus, String... variables) {
        ResponseFormat actualResponse = e.getResponseFormat() != null ?
                e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
        assertResponse(actualResponse, expectedStatus, variables);
    }

    protected void assertResponse(ResponseFormat actualResponse, ActionStatus expectedStatus, String... variables) {
        ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        assertEquals("assert error description", expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
    }

}
