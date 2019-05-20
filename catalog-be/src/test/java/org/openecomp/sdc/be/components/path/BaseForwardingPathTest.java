package org.openecomp.sdc.be.components.path;

import com.google.common.collect.Lists;
import fj.data.Either;
import org.junit.Before;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.path.beans.JanusGraphTestSetup;
import org.openecomp.sdc.be.components.path.utils.GraphTestUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.tosca.CapabilityRequirementConverter;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseForwardingPathTest extends BeConfDependentTest implements ForwardingPathTestUtils {


    protected User user;
    private ForwardingPathDataDefinition forwardingPathDataDefinition;

    @Autowired
    protected JanusGraphClient janusGraphClient;

    @Autowired
    protected CapabilityRequirementConverter capabiltyRequirementConvertor;

    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    @Autowired
    protected ServiceBusinessLogic bl;

    @Autowired
    protected IElementOperation elementDao;

    @Autowired
    protected ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @javax.annotation.Resource
    protected JanusGraphDao janusGraphDao;

    @Before
    public void initJanusGraph() {
        JanusGraphTestSetup.createGraph(janusGraphClient.getGraph().left().value());
        categoryDefinition = new CategoryDefinition();
        categoryDefinition.setName(CATEGORY_NAME);
    }

    @Before
    public void initUser() {
        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());
    }


    private CategoryDefinition categoryDefinition;
    private static final String CATEGORY_NAME = "cat_name";
    static final String FORWARDING_PATH_ID = "forwarding_pathId";
    static final String HTTP_PROTOCOL = "http";
    private static final String INSTANTIATION_TYPE = "A-la-carte";
    private static final String CAPABILITY_NAME_1 = "CP1";
    private static final String CAPABILITY_NAME_2 = "CP2";
    private static final String CAPABILITY_NAME_3 = "CP3";
    private static final String CI_NAME_1 = "CI1";
    private static final String CI_NAME_2 = "CI2";
    private static final String CI_NAME_3 = "CI3";


    private void initGraph() {
        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, "org.openecomp.resource.abstract.nodes.service");

        GraphTestUtils.createServiceVertex(janusGraphDao, props);

        GraphVertex resourceVertex = GraphTestUtils.createResourceVertex(janusGraphDao, props, ResourceTypeEnum.PNF);
        resourceVertex.setJsonMetadataField(JsonPresentationFields.VERSION, "0.1");
        Either<GraphVertex, JanusGraphOperationStatus> vertexJanusGraphOperationStatusEither = janusGraphDao
            .updateVertex(resourceVertex);
        assertTrue(vertexJanusGraphOperationStatusEither.isLeft());
    }

    private Service createTestService() {
        createCategory();
        createServiceCategory(CATEGORY_NAME);
        initGraph();
        Service service = new Service();
        service.setName("ForwardingPathTestingService");
        service.setDescription("Just a comment.");
        service.setTags(Lists.newArrayList(service.getName(), service.getComponentType().getValue() + service.getName() + "2"));
        service.setContactId("as123y");
        service.setIcon("MyIcon");
        service.setProjectCode("414155");
        service.setInstantiationType(INSTANTIATION_TYPE);
        ArrayList<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition cd = new CategoryDefinition();
        cd.setName(CATEGORY_NAME);
        cd.setNormalizedName("abcde");
        categories.add(cd);
        service.setCategories(categories);
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        service.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_APPROVED);
        return service;
    }

    private void createCategory() {
        Either<CategoryDefinition, ActionStatus> category = elementDao.createCategory(categoryDefinition, NodeTypeEnum.ServiceNewCategory);
        assertTrue("Failed to create category", category.isLeft());
    }

    private void createServiceCategory(String categoryName) {
        GraphVertex cat = new GraphVertex(VertexTypeEnum.SERVICE_CATEGORY);
        Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
        String catId = UniqueIdBuilder.buildComponentCategoryUid(categoryName, VertexTypeEnum.SERVICE_CATEGORY);
        cat.setUniqueId(catId);
        metadataProperties.put(GraphPropertyEnum.UNIQUE_ID, catId);
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.SERVICE_CATEGORY.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, categoryName);
        metadataProperties.put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
        cat.setMetadataProperties(metadataProperties);
        cat.updateMetadataJsonWithCurrentMetadataProperties();

        Either<GraphVertex, JanusGraphOperationStatus> catRes = janusGraphDao.createVertex(cat);

        assertTrue(catRes.isLeft());
    }

    Service initForwardPath() {
        ForwardingPathDataDefinition forwardingPathDataDefinition = createMockPath();
        Service service = new Service();
        service.setUniqueId(FORWARDING_PATH_ID);
        assertNull(service.addForwardingPath(forwardingPathDataDefinition));
        return service;
    }

    private ForwardingPathDataDefinition createMockPath() {
        if (forwardingPathDataDefinition != null) {
            return forwardingPathDataDefinition;
        }
        forwardingPathDataDefinition = new ForwardingPathDataDefinition("Yoyo");
        forwardingPathDataDefinition.setUniqueId(java.util.UUID.randomUUID().toString());
        forwardingPathDataDefinition.setDestinationPortNumber("414155");
        forwardingPathDataDefinition.setProtocol(HTTP_PROTOCOL);
        org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition> forwardingPathElementDataDefinitionListDataDefinition = new org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<>();
        forwardingPathElementDataDefinitionListDataDefinition.add(new ForwardingPathElementDataDefinition(CI_NAME_1, CI_NAME_2, CAPABILITY_NAME_1, CAPABILITY_NAME_2, "2222", "5555"));
        forwardingPathElementDataDefinitionListDataDefinition.add(new ForwardingPathElementDataDefinition(CI_NAME_2, CI_NAME_3, CAPABILITY_NAME_2, CAPABILITY_NAME_3, "4", "44"));
        forwardingPathDataDefinition.setPathElements(forwardingPathElementDataDefinitionListDataDefinition);
        return forwardingPathDataDefinition;
    }

    Service createService() {
        Either<Service, ResponseFormat> serviceCreateResult = bl.createService(createTestService(), user);
        assertTrue("Failed to create service", serviceCreateResult.isLeft());
        return serviceCreateResult.left().value();
    }
}

