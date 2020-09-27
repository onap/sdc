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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.mockito.Mock;
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
import org.openecomp.sdc.be.components.validation.component.*;
import org.openecomp.sdc.be.components.validation.service.*;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.*;
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
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.UploadArtifactInfo;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
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
    protected CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic = Mockito.mock(CsarArtifactsAndGroupsBusinessLogic.class);
    protected GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
    protected UserValidations userValidations = Mockito.mock(UserValidations.class);
    protected ResourceAdminEvent auditArchive1 = Mockito.mock(ResourceAdminEvent.class);
    protected CatalogOperation catalogOperation = Mockito.mock(CatalogOperation.class);
    protected ResourceAdminEvent auditArchive2 = Mockito.mock(ResourceAdminEvent.class);
    protected ResourceAdminEvent auditRestore = Mockito.mock(ResourceAdminEvent.class);
    protected ServiceImportParseLogic serviceImportParseLogic = Mockito.mock(ServiceImportParseLogic.class);
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

        when(serviceImportParseLogic.isArtifactDeletionRequired(anyString(),any(),anyBoolean())).thenReturn(true);
        Either<Boolean, ResponseFormat> validateCGD = Either.left(true);
        when(serviceImportParseLogic.validateCyclicGroupsDependencies(any())).thenReturn(validateCGD);

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


    protected Map<String, GroupDefinition> getGroups() {
        Map<String, GroupDefinition> groups = new HashMap<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName("groupDefinitionName");
        groups.put("groupsMap",groupDefinition);
        return groups;
    }

    protected UploadComponentInstanceInfo getuploadComponentInstanceInfo(){
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
        uploadReqInfo.setCapabilityName("tosca.capabilities.Node");
        uploadReqInfoList.add(uploadReqInfo);
        requirements.put("requirements",uploadReqInfoList);
        uploadNodeFilterInfo.setName("mme_ipu_vdu.virtualbinding");
        uploadComponentInstanceInfo.setCapabilities(getCapabilities());
        uploadComponentInstanceInfo.setRequirements(requirements);
        uploadComponentInstanceInfo.setName("zxjTestImportServiceAb");
        return uploadComponentInstanceInfo;
    }

    protected Map<String, List<UploadCapInfo>> getCapabilities(){
        List<UploadCapInfo> uploadCapInfoList = new ArrayList<>();
        UploadCapInfo uploadCapInfo = new UploadCapInfo();
        uploadCapInfo.setNode("tosca.nodes.Root");
        uploadCapInfo.setName("mme_ipu_vdu.dependency");
        uploadCapInfoList.add(uploadCapInfo);
        Map<String, List<UploadCapInfo>> uploadCapInfoMap = new HashMap<>();
        uploadCapInfoMap.put("tosca.capabilities.Node",uploadCapInfoList);
        return uploadCapInfoMap;
    }

    protected List<ComponentInstance> creatComponentInstances(){
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> capabilityDefinitionList = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("mme_ipu_vdu.feature");
        capabilityDefinitionList.add(capabilityDefinition);
        capabilities.put("tosca.capabilities.Node",capabilityDefinitionList);

        Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        List<RequirementDefinition> requirementDefinitionList = new ArrayList<>();
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setName("zxjtestimportserviceab0.mme_ipu_vdu.dependency.test");
        requirementDefinitionList.add(requirementDefinition);
        requirements.put("tosca.capabilities.Node",requirementDefinitionList);
        componentInstance.setRequirements(requirements);
        componentInstance.setCapabilities(capabilities);
        componentInstance.setUniqueId("uniqueId");
        componentInstance.setComponentUid("componentUid");
        componentInstance.setName("zxjTestImportServiceAb");
        componentInstances.add(componentInstance);
        return componentInstances;
    }

    protected UploadComponentInstanceInfo createUploadComponentInstanceInfo(){
        UploadComponentInstanceInfo uploadComponentInstanceInfo = new UploadComponentInstanceInfo();
        uploadComponentInstanceInfo.setName("UploadComponentInstanceInfo");
        return uploadComponentInstanceInfo;
    }

    private void mockAbstract() {
        checkCreateAbstract();
        checkCreateOther();
        checkCreateFile();
    }

    private void checkCreateAbstract(){
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

    private void checkCreateOther(){
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

    protected void checkGetUploadServiceInfo(){
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

    private void checkCreateFile(){
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

    protected CsarInfo getCsarInfo ()
    {
        String csarUuid = "0010";
        User user = new User();
        Map<String, byte[]> csar = crateCsarFromPayload();
        String vfReousrceName = "resouceName";
        String mainTemplateName = "mainTemplateName";
        String mainTemplateContent = getMainTemplateContent();
        final Service service = createServiceObject(false);
        CsarInfo csarInfo = new CsarInfo(user, csarUuid,  csar, vfReousrceName, mainTemplateName, mainTemplateContent, false);
        return csarInfo;
    }

    protected Map<String, byte[]> crateCsarFromPayload() {
        String payloadName = "valid_vf.csar";
        byte[] data = new byte[1024];
        Map<String, byte[]> returnValue = new HashMap<>();
        returnValue.put(payloadName,data);

        return returnValue;
    }

    protected String getGroupsYaml(){
        return "zxjTestImportServiceAb 0:\n" +
                "      type: org.openecomp.resource.vf.Zxjtestimportserviceab\n" +
                "      metadata:\n" +
                "        invariantUUID: 41474f7f-3195-443d-a0a2-eb6020a56279\n" +
                "        UUID: 92e32e49-55f8-46bf-984d-a98c924037ec\n" +
                "        customizationUUID: 40286158-96d0-408e-9f27-21d43817d37c\n" +
                "        version: '1.0'\n" +
                "        name: zxjTestImportServiceAb\n" +
                "        description: zxjTestImportServiceAbstract\n" +
                "        type: VF\n" +
                "        category: Generic\n" +
                "        subcategory: Abstract\n" +
                "        resourceVendor: zxjImportService\n" +
                "        resourceVendorRelease: '1.0'\n" +
                "        resourceVendorModelNumber: ''\n" +
                "      properties:\n" +
                "        skip_post_instantiation_configuration: true\n" +
                "        nf_naming:\n" +
                "          ecomp_generated_naming: true\n" +
                "        multi_stage_design: 'false'\n" +
                "        controller_actor: SO-REF-DATA\n" +
                "        availability_zone_max_count: 1\n" +
                "      requirements:\n" +
                "      - imagefile.dependency:\n" +
                "          capability: feature\n" +
                "          node: ext ZTE VL 0\n" +
                "      - mme_ipu_vdu.dependency:\n" +
                "          capability: feature\n" +
                "          node: ExtCP 0\n" +
                "      capabilities:\n" +
                "        mme_ipu_vdu.scalable:\n" +
                "          properties:\n" +
                "            max_instances: 1\n" +
                "            min_instances: 1\n" +
                "        mme_ipu_vdu.nfv_compute:\n" +
                "          properties:\n" +
                "            num_cpus: '2'\n" +
                "            flavor_extra_specs: {\n" +
                "              }\n" +
                "            mem_size: '8192'";
    }

    protected String getYamlFileContent(){
        return "tosca_definitions_version: tosca_simple_yaml_1_1\n" +
                "imports:\n" +
                "- data.yml\n" +
                "group_types:\n" +
                "  tosca.groups.Root:\n" +
                "    description: The TOSCA Group Type all other TOSCA Group Types derive from\n" +
                "    interfaces:\n" +
                "      Standard:\n" +
                "        type: tosca.interfaces.node.lifecycle.Standard\n" +
                "  org.openecomp.groups.heat.HeatStack:\n" +
                "    derived_from: tosca.groups.Root\n" +
                "    description: Grouped all heat resources which are in the same heat stack\n" +
                "    properties:\n" +
                "      heat_file:\n" +
                "        type: string\n" +
                "        description: Heat file which associate to this group/heat stack\n" +
                "        required: true\n" +
                "        status: supported\n" +
                "      description:\n" +
                "        type: string\n" +
                "        description: group description\n" +
                "        required: true\n" +
                "        status: supported\n" +
                "  org.openecomp.groups.VfModule:\n" +
                "    derived_from: tosca.groups.Root\n" +
                "    description: Grouped all heat resources which are in the same VF Module\n" +
                "    properties:\n" +
                "      isBase:\n" +
                "        type: boolean\n" +
                "        description: Whether this module should be deployed before other modules\n" +
                "        required: true\n" +
                "        default: false\n" +
                "        status: supported\n" +
                "      vf_module_label:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: |\n" +
                "          Alternate textual key used to reference this VF-Module model. Must be unique within the VNF model\n" +
                "      vf_module_description:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: |\n" +
                "          Description of the VF-modules contents and purpose (e.g. \"Front-End\" or \"Database Cluster\")\n" +
                "      min_vf_module_instances:\n" +
                "        type: integer\n" +
                "        required: true\n" +
                "        description: The minimum instances of this VF-Module\n" +
                "      max_vf_module_instances:\n" +
                "        type: integer\n" +
                "        required: false\n" +
                "        description: The maximum instances of this VF-Module\n" +
                "      initial_count:\n" +
                "        type: integer\n" +
                "        required: false\n" +
                "        description: |\n" +
                "          The initial count of instances of the VF-Module. The value must be in the range between min_vfmodule_instances and max_vfmodule_instances. If no value provided the initial count is the min_vfmodule_instances.\n" +
                "      vf_module_type:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        constraint:\n" +
                "        - valid_values:\n" +
                "          - Base\n" +
                "          - Expansion\n" +
                "      volume_group:\n" +
                "        type: boolean\n" +
                "        required: true\n" +
                "        default: false\n" +
                "        description: |\n" +
                "          \"true\" indicates that this VF Module model requires attachment to a Volume Group. VID operator must select the Volume Group instance to attach to a VF-Module at deployment time.\n" +
                "      availability_zone_count:\n" +
                "        type: integer\n" +
                "        required: false\n" +
                "        description: |\n" +
                "          Quantity of Availability Zones needed for this VF-Module (source: Extracted from VF-Module HEAT template)\n" +
                "      vfc_list:\n" +
                "        type: map\n" +
                "        entry_schema:\n" +
                "          description: <vfc_id>:<count>\n" +
                "          type: string\n" +
                "        required: false\n" +
                "        description: |\n" +
                "          Identifies the set of VM types and their count included in the VF-Module\n" +
                "  org.openecomp.groups.NetworkCollection:\n" +
                "    derived_from: tosca.groups.Root\n" +
                "    description: groups l3-networks in network collection\n" +
                "    properties:\n" +
                "      network_collection_function:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: network collection function\n" +
                "      network_collection_description:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: network collection description, free format text\n" +
                "  org.openecomp.groups.VfcInstanceGroup:\n" +
                "    derived_from: tosca.groups.Root\n" +
                "    description: groups VFCs with same parent port role\n" +
                "    properties:\n" +
                "      vfc_instance_group_function:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: function of this VFC group\n" +
                "      vfc_parent_port_role:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: common role of parent ports of VFCs in this group\n" +
                "      network_collection_function:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: network collection function assigned to this group\n" +
                "      subinterface_role:\n" +
                "        type: string\n" +
                "        required: true\n" +
                "        description: common role of subinterfaces of VFCs in this group, criteria the group is created\n" +
                "    capabilities:\n" +
                "      vlan_assignment:\n" +
                "        type: org.openecomp.capabilities.VLANAssignment\n" +
                "        properties:\n" +
                "          vfc_instance_group_reference:\n" +
                "            type: string\n" +
                "  tosca.groups.nfv.PlacementGroup:\n" +
                "    derived_from: tosca.groups.Root\n" +
                "    description: PlacementGroup is used for describing the affinity or anti-affinity relationship applicable between the virtualization containers to be created based on different VDUs, or between internal VLs to be created based on different VnfVirtualLinkDesc(s)\n" +
                "    properties:\n" +
                "      description:\n" +
                "        type: string\n" +
                "        description: Human readable description of the group\n" +
                "        required: true\n" +
                "    members:\n" +
                "    - tosca.nodes.nfv.Vdu.Compute\n" +
                "    - tosca.nodes.nfv.VnfVirtualLink";
    }

    public String getMainTemplateContent(){
        return "tosca_definitions_version: tosca_simple_yaml_1_1\n"
                + "metadata:\n"
                + "  invariantUUID: 6d17f281-683b-4198-a676-0faeecdc9025\n"
                + "  UUID: bfeab6b4-199b-4a2b-b724-de416c5e9811\n"
                + "  name: ser09080002\n"
                + "  description: ser09080002\n"
                + "  type: Service\n"
                + "  category: E2E Service\n"
                + "  serviceType: ''\n"
                + "  serviceRole: ''\n"
                + "  instantiationType: A-la-carte\n"
                + "  serviceEcompNaming: true\n"
                + "  ecompGeneratedNaming: true\n"
                + "  namingPolicy: ''\n"
                + "  environmentContext: General_Revenue-Bearing\n"
                + "  serviceFunction: ''\n"
                + "imports:\n"
                + "- nodes:\n"
                + "    file: nodes.yml\n"
                + "- datatypes:\n"
                + "    file: data.yml\n"
                + "- capabilities:\n"
                + "    file: capabilities.yml\n"
                + "- relationships:\n"
                + "    file: relationships.yml\n"
                + "- groups:\n"
                + "    file: groups.yml\n"
                + "- policies:\n"
                + "    file: policies.yml\n"
                + "- annotations:\n"
                + "    file: annotations.yml\n"
                + "- service-ser09080002-interface:\n"
                + "    file: service-Ser09080002-template-interface.yml\n"
                + "- resource-ExtCP:\n"
                + "    file: resource-Extcp-template.yml\n"
                + "- resource-zxjTestImportServiceAb:\n"
                + "    file: resource-Zxjtestimportserviceab-template.yml\n"
                + "- resource-zxjTestImportServiceAb-interface:\n"
                + "    file: resource-Zxjtestimportserviceab-template-interface.yml\n"
                + "- resource-zxjTestServiceNotAbatract:\n"
                + "    file: resource-Zxjtestservicenotabatract-template.yml\n"
                + "- resource-zxjTestServiceNotAbatract-interface:\n"
                + "    file: resource-Zxjtestservicenotabatract-template-interface.yml\n"
                + "- resource-ext ZTE VL:\n"
                + "    file: resource-ExtZteVl-template.yml\n"
                + "topology_template:\n"
                + "  inputs:\n"
                + "    skip_post_instantiation_configuration:\n"
                + "      default: true\n"
                + "      type: boolean\n"
                + "      required: false\n"
                + "    controller_actor:\n"
                + "      default: SO-REF-DATA\n"
                + "      type: string\n"
                + "      required: false\n"
                + "    cds_model_version:\n"
                + "      type: string\n"
                + "      required: false\n"
                + "    cds_model_name:\n"
                + "      type: string\n"
                + "      required: false\n"
                + "  node_templates:\n"
                + "    ext ZTE VL 0:\n"
                + "      type: tosca.nodes.nfv.ext.zte.VL\n"
                + "      metadata:\n"
                + "        invariantUUID: 27ab7610-1a97-4daa-938a-3b48e7afcfd0\n"
                + "        UUID: 9ea63e2c-4b8a-414f-93e3-5703ca5cee0d\n"
                + "        customizationUUID: e45e79b0-07ab-46b4-ac26-1e9f155ce53c\n"
                + "        version: '1.0'\n"
                + "        name: ext ZTE VL\n"
                + "        description: Ext ZTE VL\n"
                + "        type: VL\n"
                + "        category: Generic\n"
                + "        subcategory: Network Elements\n"
                + "        resourceVendor: ONAP (Tosca)\n"
                + "        resourceVendorRelease: 1.0.0.wd03\n"
                + "        resourceVendorModelNumber: ''\n"
                + "    zxjTestServiceNotAbatract 0:\n"
                + "      type: org.openecomp.resource.vf.Zxjtestservicenotabatract\n"
                + "      metadata:\n"
                + "        invariantUUID: ce39ce8d-6f97-4e89-8555-ae6789cdcf1c\n"
                + "        UUID: 4ac822be-f1ae-4ace-a4b8-bf6b5d977005\n"
                + "        customizationUUID: ee34e1e8-68e2-480f-8ba6-f257bbe90d6a\n"
                + "        version: '1.0'\n"
                + "        name: zxjTestServiceNotAbatract\n"
                + "        description: zxjTestServiceNotAbatract\n"
                + "        type: VF\n"
                + "        category: Network L4+\n"
                + "        subcategory: Common Network Resources\n"
                + "        resourceVendor: zxjImportService\n"
                + "        resourceVendorRelease: '1.0'\n"
                + "        resourceVendorModelNumber: ''\n"
                + "      properties:\n"
                + "        nf_naming:\n"
                + "          ecomp_generated_naming: true\n"
                + "        skip_post_instantiation_configuration: true\n"
                + "        multi_stage_design: 'false'\n"
                + "        controller_actor: SO-REF-DATA\n"
                + "        availability_zone_max_count: 1\n"
                + "      capabilities:\n"
                + "        mme_ipu_vdu.scalable:\n"
                + "          properties:\n"
                + "            max_instances: 1\n"
                + "            min_instances: 1\n"
                + "        mme_ipu_vdu.nfv_compute:\n"
                + "          properties:\n"
                + "            num_cpus: '2'\n"
                + "            flavor_extra_specs: {\n"
                + "              }\n"
                + "            mem_size: '8192'\n"
                + "    ExtCP 0:\n"
                + "      type: org.openecomp.resource.cp.extCP\n"
                + "      metadata:\n"
                + "        invariantUUID: 9b772728-93f5-424f-bb07-f4cae2783614\n"
                + "        UUID: 424ac220-4864-453e-b757-917fe4568ff8\n"
                + "        customizationUUID: 6e65d8a8-4379-4693-87aa-82f9e34b92fd\n"
                + "        version: '1.0'\n"
                + "        name: ExtCP\n"
                + "        description: The AT&T Connection Point base type all other CP derive from\n"
                + "        type: CP\n"
                + "        category: Generic\n"
                + "        subcategory: Network Elements\n"
                + "        resourceVendor: ONAP (Tosca)\n"
                + "        resourceVendorRelease: 1.0.0.wd03\n"
                + "        resourceVendorModelNumber: ''\n"
                + "      properties:\n"
                + "        mac_requirements:\n"
                + "          mac_count_required:\n"
                + "            is_required: false\n"
                + "        exCP_naming:\n"
                + "          ecomp_generated_naming: true\n"
                + "    zxjTestImportServiceAb 0:\n"
                + "      type: org.openecomp.resource.vf.Zxjtestimportserviceab\n"
                + "      metadata:\n"
                + "        invariantUUID: 41474f7f-3195-443d-a0a2-eb6020a56279\n"
                + "        UUID: 92e32e49-55f8-46bf-984d-a98c924037ec\n"
                + "        customizationUUID: 98c7a6c7-a867-45fb-8597-dd464f98e4aa\n"
                + "        version: '1.0'\n"
                + "        name: zxjTestImportServiceAb\n"
                + "        description: zxjTestImportServiceAbstract\n"
                + "        type: VF\n"
                + "        category: Generic\n"
                + "        subcategory: Abstract\n"
                + "        resourceVendor: zxjImportService\n"
                + "        resourceVendorRelease: '1.0'\n"
                + "        resourceVendorModelNumber: ''\n"
                + "      properties:\n"
                + "        nf_naming:\n"
                + "          ecomp_generated_naming: true\n"
                + "        skip_post_instantiation_configuration: true\n"
                + "        multi_stage_design: 'false'\n"
                + "        controller_actor: SO-REF-DATA\n"
                + "        availability_zone_max_count: 1\n"
                + "      requirements:\n"
                + "      - mme_ipu_vdu.dependency:\n"
                + "          capability: feature\n"
                + "          node: ExtCP 0\n"
                + "      - imagefile.dependency:\n"
                + "          capability: feature\n"
                + "          node: ext ZTE VL 0\n"
                + "      capabilities:\n"
                + "        mme_ipu_vdu.scalable:\n"
                + "          properties:\n"
                + "            max_instances: 1\n"
                + "            min_instances: 1\n"
                + "        mme_ipu_vdu.nfv_compute:\n"
                + "          properties:\n"
                + "            num_cpus: '2'\n"
                + "            flavor_extra_specs: {\n"
                + "              }\n"
                + "            mem_size: '8192'\n"
                + "  substitution_mappings:\n"
                + "    node_type: org.openecomp.service.Ser09080002\n"
                + "    capabilities:\n"
                + "      extcp0.feature:\n"
                + "      - ExtCP 0\n"
                + "      - feature\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.monitoring_parameter:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.monitoring_parameter\n"
                + "      zxjtestimportserviceab0.imagefile.guest_os:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.guest_os\n"
                + "      zxjtestimportserviceab0.imagefile.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.feature\n"
                + "      zxjtestservicenotabatract0.imagefile.guest_os:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.guest_os\n"
                + "      zxjtestimportserviceab0.ipu_cpd.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.feature\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.virtualbinding:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.virtualbinding\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.feature\n"
                + "      extztevl0.feature:\n"
                + "      - ext ZTE VL 0\n"
                + "      - feature\n"
                + "      zxjtestimportserviceab0.imagefile.image_fle:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.image_fle\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.monitoring_parameter:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.monitoring_parameter\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.feature\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.nfv_compute:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.nfv_compute\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.scalable:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.scalable\n"
                + "      extcp0.internal_connectionPoint:\n"
                + "      - ExtCP 0\n"
                + "      - internal_connectionPoint\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.virtualbinding:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.virtualbinding\n"
                + "      zxjtestservicenotabatract0.imagefile.image_fle:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.image_fle\n"
                + "      extztevl0.virtual_linkable:\n"
                + "      - ext ZTE VL 0\n"
                + "      - virtual_linkable\n"
                + "      zxjtestservicenotabatract0.imagefile.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.feature\n"
                + "      zxjtestimportserviceab0.localstorage.feature:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - localstorage.feature\n"
                + "      zxjtestservicenotabatract0.localstorage.local_attachment:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - localstorage.local_attachment\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.scalable:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.scalable\n"
                + "      zxjtestservicenotabatract0.localstorage.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - localstorage.feature\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.nfv_compute:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.nfv_compute\n"
                + "      zxjtestimportserviceab0.localstorage.local_attachment:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - localstorage.local_attachment\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.feature:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.feature\n"
                + "      zxjtestimportserviceab0.ipu_cpd.forwarder:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.forwarder\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.forwarder:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.forwarder\n"
                + "    requirements:\n"
                + "      zxjtestservicenotabatract0.imagefile.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - imagefile.dependency\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.local_storage:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.local_storage\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.dependency\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.volume_storage:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.volume_storage\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.virtualbinding:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.virtualbinding\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.dependency\n"
                + "      zxjtestservicenotabatract0.localstorage.dependency:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - localstorage.dependency\n"
                + "      zxjtestimportserviceab0.imagefile.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - imagefile.dependency\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.volume_storage:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.volume_storage\n"
                + "      zxjtestimportserviceab0.ipu_cpd.virtualbinding:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.virtualbinding\n"
                + "      extcp0.virtualLink:\n"
                + "      - ExtCP 0\n"
                + "      - virtualLink\n"
                + "      extcp0.virtualBinding:\n"
                + "      - ExtCP 0\n"
                + "      - virtualBinding\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.guest_os:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.guest_os\n"
                + "      extcp0.dependency:\n"
                + "      - ExtCP 0\n"
                + "      - dependency\n"
                + "      zxjtestimportserviceab0.localstorage.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - localstorage.dependency\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.virtualLink:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.virtualLink\n"
                + "      extztevl0.dependency:\n"
                + "      - ext ZTE VL 0\n"
                + "      - dependency\n"
                + "      zxjtestimportserviceab0.ipu_cpd.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.dependency\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.dependency:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.dependency\n"
                + "      zxjtestimportserviceab0.mme_ipu_vdu.local_storage:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - mme_ipu_vdu.local_storage\n"
                + "      zxjtestimportserviceab0.ipu_cpd.virtualLink:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.virtualLink\n"
                + "      extcp0.external_virtualLink:\n"
                + "      - ExtCP 0\n"
                + "      - external_virtualLink\n"
                + "      zxjtestservicenotabatract0.mme_ipu_vdu.guest_os:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - mme_ipu_vdu.guest_os\n"
                + "      zxjtestimportserviceab0.ipu_cpd.forwarder:\n"
                + "      - zxjTestImportServiceAb 0\n"
                + "      - ipu_cpd.forwarder\n"
                + "      zxjtestservicenotabatract0.ipu_cpd.forwarder:\n"
                + "      - zxjTestServiceNotAbatract 0\n"
                + "      - ipu_cpd.forwarder\n";
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
