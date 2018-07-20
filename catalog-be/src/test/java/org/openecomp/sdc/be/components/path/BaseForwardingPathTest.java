package org.openecomp.sdc.be.components.path;

import com.google.common.collect.Lists;
import fj.data.Either;
import org.junit.Before;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.path.beans.TitanGraphTestSetup;
import org.openecomp.sdc.be.components.path.utils.GraphTestUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.tosca.CapabiltyRequirementConvertor;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseForwardingPathTest extends BeConfDependentTest implements ForwardingPathTestUtils {

    protected User user;
    protected ForwardingPathDataDefinition forwardingPathDataDefinition;

    @Autowired
    protected TitanGraphClient titanGraphClient;

    @Autowired
    protected CapabiltyRequirementConvertor capabiltyRequirementConvertor;

    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    @Autowired
    protected ServiceBusinessLogic bl;

    @Autowired
    protected IElementOperation elementDao;

    @Autowired
    protected ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @javax.annotation.Resource
    protected TitanDao titanDao;

    @Before
    public void initTitan() {
        TitanGraphTestSetup.createGraph(titanGraphClient.getGraph().left().value());
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


    protected CategoryDefinition categoryDefinition;
    protected static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";
    protected static final String CATEGORY_NAME = "cat_name";
    protected static final String FORWARDING_PATH_ID = "forwarding_pathId";
    protected static final String HTTP_PROTOCOL = "http";


    protected Resource setupGenericServiceMock() {
        Resource genericService = new Resource();
        genericService.setVersion("1.0");
        genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
        return genericService;
    }

    protected void initGraph() {
        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, "org.openecomp.resource.abstract.nodes.service");

        GraphTestUtils.createServiceVertex(titanDao, props);

        GraphVertex resourceVertex = GraphTestUtils.createResourceVertex(titanDao, props, ResourceTypeEnum.PNF);
        resourceVertex.setJsonMetadataField(JsonPresentationFields.VERSION, "0.1");
        Either<GraphVertex, TitanOperationStatus> vertexTitanOperationStatusEither = titanDao.updateVertex(resourceVertex);
        assertTrue(vertexTitanOperationStatusEither.isLeft());
    }

    protected Service createTestService() {
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

    protected void createCategory() {
        Either<CategoryDefinition, ActionStatus> category = elementDao.createCategory(categoryDefinition, NodeTypeEnum.ServiceNewCategory);
        assertTrue("Failed to create category", category.isLeft());
    }

    protected void createServiceCategory(String categoryName) {
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

        Either<GraphVertex, TitanOperationStatus> catRes = titanDao.createVertex(cat);

        assertTrue(catRes.isLeft());
    }

    protected Service initForwardPath() {
        ForwardingPathDataDefinition forwardingPathDataDefinition = createMockPath();
        Service service = new Service();
        service.setUniqueId(FORWARDING_PATH_ID);
        assertEquals(null, service.addForwardingPath(forwardingPathDataDefinition));
        return service;
    }

    protected ForwardingPathDataDefinition createMockPath() {
        if (forwardingPathDataDefinition != null) {
            return forwardingPathDataDefinition;
        }
        forwardingPathDataDefinition = new ForwardingPathDataDefinition("Yoyo");
        forwardingPathDataDefinition.setUniqueId(java.util.UUID.randomUUID().toString());
        forwardingPathDataDefinition.setDestinationPortNumber("414155");
        forwardingPathDataDefinition.setProtocol(HTTP_PROTOCOL);
        org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition> forwardingPathElementDataDefinitionListDataDefinition = new org.openecomp.sdc.be.datatypes.elements.ListDataDefinition<>();
        forwardingPathElementDataDefinitionListDataDefinition.add(new ForwardingPathElementDataDefinition("fromNode", "toNode", "333", "444", "2222", "5555"));
        forwardingPathElementDataDefinitionListDataDefinition.add(new ForwardingPathElementDataDefinition("toNode", "toNode2", "4444", "44444", "4", "44"));
        forwardingPathDataDefinition.setPathElements(forwardingPathElementDataDefinitionListDataDefinition);
        return forwardingPathDataDefinition;
    }

    protected Service createService() {
        Either<Service, ResponseFormat> serviceCreateResult = bl.createService(createTestService(), user);
        assertTrue("Failed to create service", serviceCreateResult.isLeft());
        Service service = serviceCreateResult.left().value();
        return service;
    }
}

