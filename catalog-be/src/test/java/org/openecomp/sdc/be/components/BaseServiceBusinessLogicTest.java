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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.junit.Before;
import org.mockito.Mockito;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.utils.ComponentBusinessLogicMock;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
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
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.springframework.web.context.WebApplicationContext;

public abstract class BaseServiceBusinessLogicTest extends ComponentBusinessLogicMock {

    protected static final String CERTIFIED_VERSION = "1.0";
    protected static final String UNCERTIFIED_VERSION = "0.2";
    protected static final String COMPONNET_ID = "myUniqueId";
    protected static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";
    private static final String SERVICE_CATEGORY = "Mobility";
    protected static Map<AuditingFieldsKey, Object> FILTER_MAP_CERTIFIED_VERSION = new HashMap<>();
    protected static Map<AuditingFieldsKey, Object> FILTER_MAP_UNCERTIFIED_VERSION_CURR = new HashMap<>();
    protected static Map<AuditingFieldsKey, Object> FILTER_MAP_UNCERTIFIED_VERSION_PREV = new HashMap<>();
    protected static ForwardingPathDataDefinition forwardingPathDataDefinition;
    protected final IDistributionEngine distributionEngine = Mockito.mock(IDistributionEngine.class);
    protected final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    protected final ServiceDistributionValidation serviceDistributionValidation = Mockito.mock(ServiceDistributionValidation.class);
    protected final ForwardingPathValidator forwardingPathValidator = Mockito.mock(ForwardingPathValidator.class);
    protected final UiComponentDataConverter uiComponentDataConverter = Mockito.mock(UiComponentDataConverter.class);
    private final ServletContext servletContext = Mockito.mock(ServletContext.class);
    private final ModelOperation modelOperation = Mockito.mock(ModelOperation.class);
    User user = null;
    Service serviceResponse = null;
    Resource genericService = null;
    private UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    private WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    private WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
    private ServiceBusinessLogic bl;
    private ResponseFormatManager responseManager = null;
    private IElementOperation mockElementDao;
    private ComponentsUtils componentsUtils;
    private AuditCassandraDao auditingDao = Mockito.mock(AuditCassandraDao.class);
    private ArtifactsBusinessLogic artifactBl = Mockito.mock(ArtifactsBusinessLogic.class);
    private GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    private JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    private ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
    private ForwardingPathOperation forwardingPathOperation = Mockito.mock(ForwardingPathOperation.class);

    protected static ForwardingPathDataDefinition createMockPath() {
        if (forwardingPathDataDefinition != null) {
            return forwardingPathDataDefinition;
        }
        forwardingPathDataDefinition = new ForwardingPathDataDefinition("Yoyo");
        forwardingPathDataDefinition.setUniqueId(java.util.UUID.randomUUID().toString());
        forwardingPathDataDefinition.setDestinationPortNumber("414155");
        forwardingPathDataDefinition.setProtocol("http");
        org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition> forwardingPathElementDataDefinitionListDataDefinition = new org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<>();
        forwardingPathElementDataDefinitionListDataDefinition.add(
            new ForwardingPathElementDataDefinition("fromNode", "toNode", "333", "444", "2222", "5555"));
        forwardingPathElementDataDefinitionListDataDefinition.add(
            new ForwardingPathElementDataDefinition("toNode", "toNode2", "4444", "44444", "4", "44"));
        forwardingPathDataDefinition.setPathElements(forwardingPathElementDataDefinitionListDataDefinition);
        return forwardingPathDataDefinition;
    }

    @Before
    public void setup() {

        ExternalConfiguration.setAppName("catalog-be");
        componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));

        // Elements
        mockElementDao = new ElementOperationMock();

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        when(mockUserAdmin.getUser("jh0003", false)).thenReturn(user);

        // Servlet Context attributes
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
//        when(servletContext.getAttribute(Constants.SERVICE_OPERATION_MANAGER)).thenReturn(new ServiceOperation());
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
        when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);
        when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);

        // artifact bussinesslogic
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        when(artifactBl.createArtifactPlaceHolderInfo(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.any(User.class),
            Mockito.any(ArtifactGroupTypeEnum.class))).thenReturn(artifactDef);

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

        //forwardingPath
        when(forwardingPathOperation.addForwardingPath(any(), any())).thenReturn(Either.left(createMockPath()));
        when(forwardingPathOperation.updateForwardingPath(any(), any())).thenReturn(Either.left(createMockPath()));
        when(forwardingPathOperation.deleteForwardingPath(any(), any())).thenReturn(Either.left(Sets.newHashSet("Wow-It-Works")));
        when(toscaOperationFacade.getToscaElement("delete_forward_test")).thenReturn(Either.left(createServiceObject(true)));

        bl = new ServiceBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
            groupTypeOperation, groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactsBusinessLogic, distributionEngine, componentInstanceBusinessLogic,
            serviceDistributionValidation, forwardingPathValidator, uiComponentDataConverter,
            artifactToscaOperation, componentContactIdValidator, componentNameValidator,
            componentTagsValidator, componentValidator, componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator,
            modelOperation);
        bl.setUserAdmin(mockUserAdmin);
        bl.setGraphLockOperation(graphLockOperation);
        bl.setJanusGraphDao(mockJanusGraphDao);
        bl.setToscaOperationFacade(toscaOperationFacade);
        bl.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
        bl.setComponentsUtils(componentsUtils);
        bl.setCassandraAuditingDao(auditingDao);
        bl.setForwardingPathOperation(forwardingPathOperation);
        bl.setToscaOperationFacade(toscaOperationFacade);
        mockAuditingDaoLogic();

        responseManager = ResponseFormatManager.getInstance();

    }

    protected Component createNewService() {

        Service service = new Service();
        int listSize = 3;
        service.setName("serviceName");
        service.setUniqueId("serviceUniqueId");
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance ci;
        for (int i = 0; i < listSize; ++i) {
            ci = new ComponentInstance();
            ci.setName("ciName" + i);
            ci.setUniqueId("ciId" + i);
            List<GroupInstance> groupInstances = new ArrayList<>();
            GroupInstance gi;
            for (int j = 0; j < listSize; ++j) {
                gi = new GroupInstance();
                gi.setName(ci.getName() + "giName" + j);
                gi.setUniqueId(ci.getName() + "giId" + j);
                groupInstances.add(gi);
            }
            ci.setGroupInstances(groupInstances);
            componentInstances.add(ci);
        }
        service.setComponentInstances(componentInstances);
        return service;
    }

    private void mockAuditingDaoLogic() {
        FILTER_MAP_CERTIFIED_VERSION.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID, COMPONNET_ID);
        FILTER_MAP_UNCERTIFIED_VERSION_CURR.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID, COMPONNET_ID);
        FILTER_MAP_UNCERTIFIED_VERSION_PREV.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID, COMPONNET_ID);

        FILTER_MAP_UNCERTIFIED_VERSION_CURR.put(AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION, UNCERTIFIED_VERSION);
        FILTER_MAP_UNCERTIFIED_VERSION_PREV.put(AuditingFieldsKey.AUDIT_RESOURCE_PREV_VERSION, UNCERTIFIED_VERSION);

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

        List<ResourceAdminEvent> listCurr = new ArrayList<ResourceAdminEvent>() {
            {
                add(checkOutResourceAudit);
            }
        };
        Either<List<ResourceAdminEvent>, ActionStatus> resultCurr = Either.left(listCurr);
        Mockito.when(auditingDao.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(resultCurr);

    }

    protected Service createServiceObject(boolean afterCreate) {
        Service service = new Service();
        service.setName("Service");
        CategoryDefinition category = new CategoryDefinition();
        category.setName(SERVICE_CATEGORY);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        service.setCategories(categories);

        service.setDescription("description");
        List<String> tgs = new ArrayList<>();
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

    protected Resource setupGenericServiceMock() {
        Resource genericService = new Resource();
        genericService.setVersion("1.0");
        genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
        return genericService;
    }
}
