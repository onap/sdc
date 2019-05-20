package org.openecomp.sdc.be.components;

import com.google.common.collect.Sets;
import fj.data.Either;
import org.junit.Before;
import org.mockito.Mockito;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ForwardingPathOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class BaseServiceBusinessLogicTest {
    private static final String SERVICE_CATEGORY = "Mobility";
    final ServletContext servletContext = Mockito.mock(ServletContext.class);
    UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
    ServiceBusinessLogic bl = new ServiceBusinessLogic();
    ResponseFormatManager responseManager = null;
    IElementOperation mockElementDao;
    ComponentsUtils componentsUtils;
    AuditCassandraDao auditingDao = Mockito.mock(AuditCassandraDao.class);
    ArtifactsBusinessLogic artifactBl = Mockito.mock(ArtifactsBusinessLogic.class);
    GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    CacheMangerOperation cacheManager = Mockito.mock(CacheMangerOperation.class);
    GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
    ForwardingPathOperation forwardingPathOperation  = Mockito.mock(ForwardingPathOperation.class);

    User user = null;
    Service serviceResponse = null;
    Resource genericService = null;

    protected static final String CERTIFIED_VERSION = "1.0";
    protected static final String UNCERTIFIED_VERSION = "0.2";
    protected static final String COMPONNET_ID = "myUniqueId";
    protected static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";
    protected static Map<AuditingFieldsKey, Object> FILTER_MAP_CERTIFIED_VERSION = new HashMap<>();
    protected static Map<AuditingFieldsKey, Object> FILTER_MAP_UNCERTIFIED_VERSION_CURR = new HashMap<>();
    protected static Map<AuditingFieldsKey, Object> FILTER_MAP_UNCERTIFIED_VERSION_PREV = new HashMap<>();
    @Before
    public void setup() {

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

        // Servlet Context attributes
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
        when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);
        when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);

        // artifact bussinesslogic
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        when(artifactBl.createArtifactPlaceHolderInfo(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.any(User.class), Mockito.any(ArtifactGroupTypeEnum.class))).thenReturn(artifactDef);

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
        when(forwardingPathOperation.addForwardingPath(any(),any())).thenReturn(Either.left(createMockPath()));
        when(forwardingPathOperation.updateForwardingPath(any(),any())).thenReturn(Either.left(createMockPath()));
        when(forwardingPathOperation.deleteForwardingPath(any(),any())).thenReturn(Either.left(Sets.newHashSet("Wow-It-Works")));
        when(toscaOperationFacade.getToscaElement("delete_forward_test")).thenReturn(Either.left(createServiceObject(true)));

        bl = new ServiceBusinessLogic();
        bl.setElementDao(mockElementDao);
        bl.setUserAdmin(mockUserAdmin);
        bl.setArtifactBl(artifactBl);
        bl.setGraphLockOperation(graphLockOperation);
        bl.setJanusGraphGenericDao(mockJanusGraphDao);
        bl.setToscaOperationFacade(toscaOperationFacade);
        bl.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
        bl.setComponentsUtils(componentsUtils);
        bl.setCassandraAuditingDao(auditingDao);
        bl.setCacheManagerOperation(cacheManager);
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
        for(int i= 0; i<listSize; ++i){
            ci = new ComponentInstance();
            ci.setName("ciName" + i);
            ci.setUniqueId("ciId" + i);
            List<GroupInstance>  groupInstances= new ArrayList<>();
            GroupInstance gi;
            for(int j = 0; j<listSize; ++j){
                gi = new GroupInstance();
                gi.setName(ci.getName( )+ "giName" + j);
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
        service.setIcon("MyIcon");
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

    protected Resource setupGenericServiceMock(){
        Resource genericService = new Resource();
        genericService.setVersion("1.0");
        genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
        return genericService;
    }

    protected static ForwardingPathDataDefinition forwardingPathDataDefinition;

    protected static ForwardingPathDataDefinition createMockPath() {
        if (forwardingPathDataDefinition != null){
            return forwardingPathDataDefinition ;
        }
        forwardingPathDataDefinition = new ForwardingPathDataDefinition("Yoyo");
        forwardingPathDataDefinition.setUniqueId(java.util.UUID.randomUUID().toString());
        forwardingPathDataDefinition.setDestinationPortNumber("414155");
        forwardingPathDataDefinition.setProtocol("http");
        org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition> forwardingPathElementDataDefinitionListDataDefinition = new org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<>();
        forwardingPathElementDataDefinitionListDataDefinition.add(new ForwardingPathElementDataDefinition("fromNode","toNode", "333","444","2222","5555"));
        forwardingPathElementDataDefinitionListDataDefinition.add(new ForwardingPathElementDataDefinition("toNode","toNode2", "4444","44444","4","44"));
        forwardingPathDataDefinition.setPathElements(forwardingPathElementDataDefinitionListDataDefinition);
        return forwardingPathDataDefinition;
    }
}
