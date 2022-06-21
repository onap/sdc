/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.utils.CreateServiceFromYamlParameter;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
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
import org.openecomp.sdc.be.externalapi.servlet.representation.AbstractResourceInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.AbstractTemplateInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.ArtifactMetadata;
import org.openecomp.sdc.be.externalapi.servlet.representation.CopyServiceInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.ReplaceVNFInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.ResourceInstanceMetadata;
import org.openecomp.sdc.be.facade.operations.CatalogOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.UploadServiceInfo;
import org.openecomp.sdc.be.model.User;
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
import org.openecomp.sdc.common.api.UploadArtifactInfo;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

public class ServiceImportBussinessLogicBaseTestSetup extends BaseBusinessLogicMock {

    protected static final String SERVICE_CATEGORY = "Mobility";
    protected static final String INSTANTIATION_TYPE = "A-la-carte";
    protected static final String CERTIFIED_VERSION = "1.0";
    protected static final String UNCERTIFIED_VERSION = "0.2";
    protected static final String COMPONNET_ID = "myUniqueId";
    protected static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";
    protected static final String SERVICE_ROLE = JsonPresentationFields.SERVICE_ROLE.getPresentation();
    protected static final String SERVICE_TYPE = JsonPresentationFields.SERVICE_TYPE.getPresentation();
    protected static final String SERVICE_FUNCTION = JsonPresentationFields.SERVICE_FUNCTION.getPresentation();
    private static final String RESOURCE_NAME = "My-Resource_Name with   space";
    private static final String RESOURCE_TOSCA_NAME = "My-Resource_Tosca_Name";
    private static final String RESOURCE_CATEGORY1 = "Network Layer 2-3";
    private static final String RESOURCE_SUBCATEGORY = "Router";
    protected final ServletContext servletContext = Mockito.mock(ServletContext.class);
    protected final ComponentValidator componentValidator = Mockito.mock(ComponentValidator.class);
    protected ServiceImportBusinessLogic sIB1;
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
    protected CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic = Mockito.mock(CsarArtifactsAndGroupsBusinessLogic.class);
    protected GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
    protected UserValidations userValidations = Mockito.mock(UserValidations.class);
    protected ResourceAdminEvent auditArchive1 = Mockito.mock(ResourceAdminEvent.class);
    protected CatalogOperation catalogOperation = Mockito.mock(CatalogOperation.class);
    protected ResourceAdminEvent auditArchive2 = Mockito.mock(ResourceAdminEvent.class);
    protected ResourceAdminEvent auditRestore = Mockito.mock(ResourceAdminEvent.class);
    protected ServiceImportParseLogic serviceImportParseLogic = Mockito.mock(ServiceImportParseLogic.class);
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
    NodeFilterOperation serviceFilterOperation = Mockito.mock(NodeFilterOperation.class);
    NodeFilterValidator serviceFilterValidator = Mockito.mock(NodeFilterValidator.class);

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

    @BeforeEach
    public void setup() throws IOException {

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
        // Servlet Context attributes
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
        when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherCreate);
        Either<Boolean, StorageOperationStatus> eitherCount = Either.left(false);
        when(toscaOperationFacade.validateComponentNameExists("Service", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCount);
        Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(true);
        when(toscaOperationFacade.validateComponentNameExists("alreadyExist", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCountExist);
        when(userValidations.validateUserExists(user)).thenReturn(user);

        // createResource
        Resource resourceRsponse = createParseResourceObject(true);
        Either<Component, StorageOperationStatus> eitherResourceCreate = Either.left(resourceRsponse);
        when(toscaOperationFacade.createToscaComponent(Mockito.any(Component.class))).thenReturn(eitherResourceCreate);
        Either<Component, StorageOperationStatus> eitherResourceRes = Either.left(resourceRsponse);
        when(toscaOperationFacade.getToscaFullElement(Mockito.anyString())).thenReturn(eitherResourceRes);

        Either<Boolean, StorageOperationStatus> eitherResourceCount = Either.left(false);
        when(toscaOperationFacade.validateComponentNameExists("Resource", null, ComponentTypeEnum.RESOURCE)).thenReturn(eitherResourceCount);
        Either<Boolean, StorageOperationStatus> eitherResourceCountExist = Either.left(true);
        when(toscaOperationFacade.validateComponentNameExists("alreadyExist", null, ComponentTypeEnum.RESOURCE)).thenReturn(eitherResourceCountExist);

        genericService = setupGenericServiceMock();
        Either<Resource, StorageOperationStatus> findLatestGeneric = Either.left(genericService);
        when(toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_SERVICE_NAME)).thenReturn(findLatestGeneric);

        when(serviceImportParseLogic.isArtifactDeletionRequired(anyString(), any(), anyBoolean())).thenReturn(true);

        sIB1 = new ServiceImportBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
            groupTypeOperation, groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactBl, distributionEngine, componentInstanceBusinessLogic,
            serviceDistributionValidation, forwardingPathValidator, uiComponentDataConverter, serviceFilterOperation,
            serviceFilterValidator, artifactToscaOperation, componentContactIdValidator,
            componentNameValidator, componentTagsValidator, componentValidator,
            componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator);

        mockAbstract();

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
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
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


    protected Map<String, GroupDefinition> getGroups() {
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName("groupDefinitionName");
        groups.put("groupsMap", groupDefinition);
        return groups;
    }

    protected UploadComponentInstanceInfo getuploadComponentInstanceInfo() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setType("resources");
        Collection<String> directives = new Collection<String>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }

            @Override
            public boolean add(String s) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends String> collection) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }
        };
        uploadComponentInstanceInfo.setDirectives(directives);
        UploadNodeFilterInfo uploadNodeFilterInfo = new UploadNodeFilterInfo();
        Map<String, List<UploadReqInfo>> requirements = new HashMap<>();
        List<UploadReqInfo> uploadReqInfoList = new ArrayList<>();
        UploadReqInfo uploadReqInfo = new UploadReqInfo();
        uploadReqInfo.setName("uploadReqInfo");
        uploadReqInfo.setNode("zxjTestImportServiceAb");
        uploadReqInfo.setCapabilityName("tosca.capabilities.Node");
        uploadReqInfoList.add(uploadReqInfo);
        requirements.put("requirements", uploadReqInfoList);
        uploadNodeFilterInfo.setName("mme_ipu_vdu.virtualbinding");
        uploadComponentInstanceInfo.setCapabilities(getCapabilities());
        uploadComponentInstanceInfo.setRequirements(requirements);
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb");
        return uploadComponentInstanceInfo;
    }

    protected Map<String, List<UploadCapInfo>> getCapabilities() {
        List<UploadCapInfo> uploadCapInfoList = new ArrayList<>();
        UploadCapInfo uploadCapInfo = new UploadCapInfo();
        uploadCapInfo.setNode("tosca.nodes.Root");
        uploadCapInfo.setName("mme_ipu_vdu.dependency");
        uploadCapInfoList.add(uploadCapInfo);
        Map<String, List<UploadCapInfo>> uploadCapInfoMap = new HashMap<>();
        uploadCapInfoMap.put("tosca.capabilities.Node", uploadCapInfoList);
        return uploadCapInfoMap;
    }

    protected List<ComponentInstance> creatComponentInstances() {
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("mme_ipu_vdu.feature");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put("tosca.capabilities.Node", capabilityDefinitionList);

        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put("tosca.capabilities.Node", requirementDefinitionList);
        componentInstance.setRequirements(requirements);
        componentInstance.setCapabilities(capabilities);
        componentInstance.setUniqueId("uniqueId");
        componentInstance.setComponentUid("componentUid");
        componentInstance.setName("zxjTestImportServiceAb");
        componentInstances.add(componentInstance);
        return componentInstances;
    }

    protected UploadComponentInstanceInfo createUploadComponentInstanceInfo() {
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("UploadComponentInstanceInfo");
        return uploadComponentInstanceInfo;
    }

    private void mockAbstract() throws IOException {
        checkCreateAbstract();
        checkCreateOther();
        checkCreateFile();
    }

    private void checkCreateAbstract() {
        AbstractResourceInfo abstractResourceInfo = new AbstractResourceInfo();
        List<RequirementCapabilityRelDef> componentInstancesRelations = new ArrayList<>();
        abstractResourceInfo.setComponentInstancesRelations(componentInstancesRelations);
        abstractResourceInfo.setAbstractResourceUniqueId("abstractResourceUniqueId");
        abstractResourceInfo.setAbstractResourceName("abstractResourceName");
        abstractResourceInfo.setAbstractResourceUUid("abstractResourceUUid");

        AbstractResourceInfo getAbstractResourceInfo = new AbstractResourceInfo();
        getAbstractResourceInfo.getAbstractResourceName();
        getAbstractResourceInfo.getAbstractResourceUniqueId();
        getAbstractResourceInfo.getAbstractResourceUUid();
        getAbstractResourceInfo.getComponentInstancesRelations();

        AbstractTemplateInfo createAbstractTemplateInfo = new AbstractTemplateInfo();
        List<AbstractResourceInfo> abstractResourceInfoList = new ArrayList<>();
        abstractResourceInfoList.add(abstractResourceInfo);
        createAbstractTemplateInfo.setAbstractResourceInfoList(abstractResourceInfoList);
        createAbstractTemplateInfo.setServiceUniqueId("serviceUniqueId");
        createAbstractTemplateInfo.setIsAbstractTemplate(true);
        createAbstractTemplateInfo.setServiceUUid("serviceUUid");

        AbstractTemplateInfo getAbstractTemplateInfo = new AbstractTemplateInfo();
        getAbstractTemplateInfo.getAbstractResourceInfoList();
        getAbstractTemplateInfo.getIsAbstractTemplate();
        getAbstractTemplateInfo.getServiceUniqueId();
        getAbstractTemplateInfo.getServiceUUid();

        CopyServiceInfo copyServiceInfo = new CopyServiceInfo();
        copyServiceInfo.setNewServiceName("newServiceName");
        copyServiceInfo.setNewServiceUUid("serviceUUid");
        copyServiceInfo.setOldServiceUUid("oldServiceUUid");

        CopyServiceInfo getCopyServiceInfo = new CopyServiceInfo();
        getCopyServiceInfo.getNewServiceName();
        getCopyServiceInfo.getNewServiceUUid();
        getCopyServiceInfo.getOldServiceUUid();

        ReplaceVNFInfo replaceVNFInfo = new ReplaceVNFInfo();
        ComponentInstance realVNFComponentInstance = new ComponentInstance();
        replaceVNFInfo.setAbstractResourceUniqueId("abstractResourceUniqueId");
        replaceVNFInfo.setRealVNFComponentInstance(realVNFComponentInstance);
        replaceVNFInfo.setServiceUniqueId("serviceUniqueId");

        ReplaceVNFInfo getReplaceVNFInfo = new ReplaceVNFInfo();
        getReplaceVNFInfo.getServiceUniqueId();
        getReplaceVNFInfo.getAbstractResourceUniqueId();
        getReplaceVNFInfo.getRealVNFComponentInstance();
    }

    private void checkCreateOther() {
        ResourceInstanceMetadata resourceInstanceMetadata = new ResourceInstanceMetadata();
        List<ArtifactMetadata> artifacts = new ArrayList<>();
        resourceInstanceMetadata.setArtifacts(artifacts);
        resourceInstanceMetadata.setResoucreType("resoucreType");
        resourceInstanceMetadata.setResourceInstanceName("resourceInstanceName");
        resourceInstanceMetadata.setResourceInvariantUUID("resourceInvariantUUID");
        resourceInstanceMetadata.setResourceName("resourceName");
        resourceInstanceMetadata.setResourceUUID("resourceUUID");
        resourceInstanceMetadata.setResourceVersion("resourceVersion");

        ResourceInstanceMetadata getResourceInstanceMetadata = new ResourceInstanceMetadata();
        getResourceInstanceMetadata.getArtifacts();
        getResourceInstanceMetadata.getResoucreType();
        getResourceInstanceMetadata.getResourceInstanceName();
        getResourceInstanceMetadata.getResourceInvariantUUID();
        getResourceInstanceMetadata.getResourceName();
        getResourceInstanceMetadata.getResourceUUID();
        getResourceInstanceMetadata.getResourceVersion();

        UploadServiceInfo uploadServiceInfo = new UploadServiceInfo();
        List<String> tags = new ArrayList<>();
        List<CategoryDefinition> categories = new ArrayList<>();
        List<UploadArtifactInfo> artifactList = new ArrayList<>();
        uploadServiceInfo.setProjectCode("projectCode");
        uploadServiceInfo.setCategories(categories);
        uploadServiceInfo.setServiceType("");
        uploadServiceInfo.setServiceVendorModelNumber("serviceVendorModelNumber");
        uploadServiceInfo.setVendorRelease("vendorRelease");
        uploadServiceInfo.setVendorName("vendorName");
        uploadServiceInfo.setServiceIconPath("serviceIconPath");
        uploadServiceInfo.setName("uploadServiceInfo");
        uploadServiceInfo.setContactId("contactId");
        uploadServiceInfo.setIcon("icon");
        uploadServiceInfo.setNamingPolicy("namingPolicy");
        uploadServiceInfo.setEcompGeneratedNaming("ecompGeneratedNaming");
        uploadServiceInfo.setServiceEcompNaming("serviceEcompNaming");
        uploadServiceInfo.setServiceRole("serviceRole");
        uploadServiceInfo.setSubcategory("subcategory");
        uploadServiceInfo.setCategory("category");
        uploadServiceInfo.setType("type");
        uploadServiceInfo.setUUID("UUID");
        uploadServiceInfo.setInvariantUUID("invariantUUID");
        uploadServiceInfo.setResourceVendorRelease("resourceVendorRelease");
        uploadServiceInfo.setResourceVendor("resourceVendor");
        uploadServiceInfo.setDescription("description");
        uploadServiceInfo.setTags(tags);
        uploadServiceInfo.setArtifactList(artifactList);
        uploadServiceInfo.setPayloadName("payloadName");
        uploadServiceInfo.setPayloadData("payloadData");
    }

    protected void checkGetUploadServiceInfo() {
        UploadServiceInfo uploadServiceInfo = new UploadServiceInfo();
        List<String> tags = new ArrayList<>();
        List<CategoryDefinition> categories = new ArrayList<>();
        List<UploadArtifactInfo> artifactList = new ArrayList<>();
        uploadServiceInfo.getProjectCode();
        uploadServiceInfo.getCategories();
        uploadServiceInfo.getServiceType();
        uploadServiceInfo.getServiceVendorModelNumber();
        uploadServiceInfo.getVendorRelease();
        uploadServiceInfo.getVendorName();
        uploadServiceInfo.getServiceIconPath();
        uploadServiceInfo.getName();
        uploadServiceInfo.getContactId();
        uploadServiceInfo.getIcon();
        uploadServiceInfo.getNamingPolicy();
        uploadServiceInfo.getEcompGeneratedNaming();
        uploadServiceInfo.getServiceEcompNaming();
        uploadServiceInfo.getServiceRole();
        uploadServiceInfo.getSubcategory();
        uploadServiceInfo.getCategory();
        uploadServiceInfo.getType();
        uploadServiceInfo.getUUID();
        uploadServiceInfo.getInvariantUUID();
        uploadServiceInfo.getResourceVendorRelease();
        uploadServiceInfo.getResourceVendor();
        uploadServiceInfo.getDescription();
        uploadServiceInfo.getTags();
        uploadServiceInfo.getArtifactList();
        uploadServiceInfo.getPayloadName();
        uploadServiceInfo.getPayloadData();
    }

    private void checkCreateFile() throws IOException {
        CreateServiceFromYamlParameter csfp = new CreateServiceFromYamlParameter();
        Map<String, NodeTypeInfo> nodeTypesInfo = new HashMap<>();
        ParsedToscaYamlInfo parsedToscaYamlInfo = new ParsedToscaYamlInfo();
        List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
        CsarInfo csarInfo = getCsarInfo();
        csfp.setYamlName("yamlName");
        csfp.setNodeTypesInfo(nodeTypesInfo);
        csfp.setParsedToscaYamlInfo(parsedToscaYamlInfo);
        csfp.setCsarInfo(csarInfo);
        csfp.setCreatedArtifacts(createdArtifacts);
        csfp.setYamlName("yamlName");
        csfp.setShouldLock(true);
        csfp.setInTransaction(true);
        csfp.setNodeName("nodeName");

        CreateServiceFromYamlParameter getCsfy = new CreateServiceFromYamlParameter();
        getCsfy.getYamlName();
        getCsfy.getNodeTypesInfo();
        getCsfy.getParsedToscaYamlInfo();
        getCsfy.getCsarInfo();
        getCsfy.getCreatedArtifacts();
        getCsfy.getYamlName();
        getCsfy.isShouldLock();
        getCsfy.isInTransaction();
        getCsfy.getNodeName();
    }

    protected CsarInfo getCsarInfo() throws IOException {
        String csarUuid = "0010";
        User user = new User();
        Map<String, byte[]> csar = crateCsarFromPayload();
        String vfReousrceName = "resouceName";
        String mainTemplateName = "mainTemplateName";
        String mainTemplateContent = getMainTemplateContent();
        final Service service = createServiceObject(false);
        CsarInfo csarInfo = new CsarInfo(user, csarUuid, csar, vfReousrceName, mainTemplateName, mainTemplateContent, false);
        return csarInfo;
    }

    protected Map<String, byte[]> crateCsarFromPayload() {
        String payloadName = "valid_vf.csar";
        byte[] data = new byte[1024];
        Map<String, byte[]> returnValue = new HashMap<>();
        returnValue.put(payloadName, data);

        return returnValue;
    }

    protected String getGroupsYaml() throws IOException {
        Path filePath = Paths.get("src/test/resources/types/groups.yml");
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }

    protected String getYamlFileContent() throws IOException {
        Path filePath = Paths.get("src/test/resources/types/fileContent.yml");
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }

    public String getMainTemplateContent() throws IOException {
        Path filePath = Paths.get("src/test/resources/types/mainTemplateContent.yml");
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }

    protected String getArtifactsYml() throws IOException {
        Path filePath = Paths.get("src/test/resources/types/artifacts.yml");
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
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
