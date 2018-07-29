package org.openecomp.sdc.be.model.jsontitan.operations;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class InterfacesOperationTest extends ModelTestBase{
    @Resource
    protected TitanDao titanDao;
    @Resource
    private InterfaceOperation interfaceOperation;

    @Autowired
    protected TitanGraphClient titanGraphClient;

    @Resource
    protected NodeTypeOperation nodeTypeOperation;
    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    @Resource
    protected TopologyTemplateOperation topologyTemplateOperation;

    @Autowired
    protected IElementOperation elementDao;

    @Resource
    private ToscaElementLifecycleOperation lifecycleOperation;

    protected static final String USER_ID = "jh0003";
    protected static final String VF_NAME  = "VF_NAME";
    protected User user;

    public static final String RESOURCE_CATEGORY = "Network Layer 2-3";
    public static final String RESOURCE_SUBCATEGORY = "Router";
    public static final String RESOURCE_NAME = "Resource Name";

    private CategoryDefinition categoryDefinition;
    private SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition();
    protected static final String RESOURCE_ID = "resourceID";
    protected static final String WORKFLOW_OPERATION_ID = "workflowOperationId";
    public static final String DERIVED_NAME = "derivedName";
    public static final String CSAR_UUID = "bla bla";


    String categoryName = "category";
    String subcategory = "mycategory";
    String outputDirectory = "C:\\Output";

    @BeforeClass
    public static void initInterfacesOperation() {
        init();
    }

    private GraphVertex ownerVertex;
    private GraphVertex modifierVertex;
    private GraphVertex vfVertex;
    private GraphVertex serviceVertex;
    private GraphVertex rootVertex;

    @Before
    public void setupBefore() {
        clearGraph();
        createUsers();
        createResourceCategory();
        createServiceCategory();
        GraphTestUtils.createRootCatalogVertex(titanDao);
        rootVertex = createRootNodeType();
        createNodeType("firstVf");
        serviceVertex = createTopologyTemplate("firstService");
    }

    @After
    public void cleanAfter() {
        clearGraph();
    }

    @Test
    public void testAddInterface() {
        org.openecomp.sdc.be.model.Resource resource = createResource();
        InterfaceDefinition interfaceDefinition = buildInterfaceDefinition();
        Either<InterfaceDefinition, StorageOperationStatus> res = interfaceOperation.addInterface(resource.getUniqueId(),
            interfaceDefinition);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    public void testUpdateInterface() {
        org.openecomp.sdc.be.model.Resource resource = createResource();
        InterfaceDefinition interfaceDefinition = buildInterfaceDefinition();
        interfaceDefinition.setOperationsMap(createMockOperationMap());
        Either<InterfaceDefinition, StorageOperationStatus> res = interfaceOperation.addInterface(resource.getUniqueId(),
            interfaceDefinition);
        Assert.assertTrue(res.isLeft());
        InterfaceDefinition value = res.left().value();
        String new_description = "New Description";
        value.setDescription(new_description);
        res = interfaceOperation.updateInterface(resource.getUniqueId(),
            interfaceDefinition);
        assertTrue(res.isLeft());
        assertEquals(new_description,res.left().value().getDescription());
    }

    @Test
    public void testUpdateInterfaceShouldFailWhenNOtCreatedFirst() {
        org.openecomp.sdc.be.model.Resource resource = createResource();
        InterfaceDefinition interfaceDefinition = buildInterfaceDefinition();
        interfaceDefinition.setOperationsMap(createMockOperationMap());
        Either<InterfaceDefinition, StorageOperationStatus> res = interfaceOperation.updateInterface(resource.getUniqueId(),
            interfaceDefinition);
        Assert.assertTrue(res.isRight());
    }

    private InterfaceDefinition buildInterfaceDefinition() {
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.standard");
        interfaceDefinition.setCreationDate(new Long(101232));


        return interfaceDefinition;
    }

    private org.openecomp.sdc.be.model.Resource createResource() {
        org.openecomp.sdc.be.model.Resource resource = new org.openecomp.sdc.be.model.Resource();
        resource.setUniqueId(RESOURCE_ID);
        resource.setName(RESOURCE_NAME);
        resource.addCategory(RESOURCE_CATEGORY, RESOURCE_SUBCATEGORY);
        resource.setDescription("My short description");
        resource.setInterfaces(createMockInterfaceDefinition());
        return resource;
    }


    private InterfaceDefinition createInterface(String uniqueID, String description, String type, String toscaResourceName,
        Map<String, Operation> op) {
        InterfaceDefinition id = new InterfaceDefinition();
        id.setType(type);
        id.setDescription(description);
        id.setUniqueId(uniqueID);
        id.setToscaResourceName(toscaResourceName);
        id.setOperationsMap(op);
        return id;
    }

    private  Map<String, InterfaceDefinition> createMockInterfaceDefinition() {
        Map<String, Operation> operationMap = createMockOperationMap();
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put("int1", createInterface("int1", "Interface 1",
            "lifecycle", "tosca", operationMap));

        return interfaceDefinitionMap;
    }

    private Map<String, Operation> createMockOperationMap() {
        Operation operation = new Operation();
        operation.setDefinition(false);
        operation.setName("create");
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put("op1", operation);
        return operationMap;
    }




    private void createResourceCategory() {

        GraphVertex cat = new GraphVertex(VertexTypeEnum.RESOURCE_CATEGORY);
        Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
        String catId = UniqueIdBuilder.buildComponentCategoryUid(categoryName, VertexTypeEnum.RESOURCE_CATEGORY);
        cat.setUniqueId(catId);
        metadataProperties.put(GraphPropertyEnum.UNIQUE_ID, catId);
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.RESOURCE_CATEGORY.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, categoryName);
        metadataProperties.put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
        cat.setMetadataProperties(metadataProperties);
        cat.updateMetadataJsonWithCurrentMetadataProperties();

        GraphVertex subCat = new GraphVertex(VertexTypeEnum.RESOURCE_SUBCATEGORY);
        metadataProperties = new HashMap<>();
        String subCatId = UniqueIdBuilder.buildSubCategoryUid(cat.getUniqueId(), subcategory);
        subCat.setUniqueId(subCatId);
        metadataProperties.put(GraphPropertyEnum.UNIQUE_ID, subCatId);
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.RESOURCE_SUBCATEGORY.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, subcategory);
        metadataProperties.put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(subcategory));
        subCat.setMetadataProperties(metadataProperties);
        subCat.updateMetadataJsonWithCurrentMetadataProperties();

        Either<GraphVertex, TitanOperationStatus> catRes = titanDao.createVertex(cat);

        Either<GraphVertex, TitanOperationStatus> subCatRes = titanDao.createVertex(subCat);

        TitanOperationStatus status = titanDao.createEdge(catRes.left().value().getVertex(), subCatRes.left().value().getVertex(), EdgeLabelEnum.SUB_CATEGORY, new HashMap<>());
        assertEquals(TitanOperationStatus.OK, status);
    }

    private void createServiceCategory() {

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

    private GraphVertex createTopologyTemplate(String name) {

        TopologyTemplate service = new TopologyTemplate();
        String uniqueId = UniqueIdBuilder.buildResourceUniqueId();
        service.setUniqueId(uniqueId);
        service.setCreatorUserId((String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID));
        service.getMetadata().put(JsonPresentationFields.NAME.getPresentation(), name);
        service.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
        service.getMetadata().put(JsonPresentationFields.VERSION.getPresentation(), "0.1");
        service.getMetadata().put(JsonPresentationFields.TYPE.getPresentation(), ResourceTypeEnum.VF.name());
        service.getMetadata().put(JsonPresentationFields.COMPONENT_TYPE.getPresentation(), ComponentTypeEnum.RESOURCE);
        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition cat = new CategoryDefinition();
        categories.add(cat);
        cat.setName(categoryName);
        service.setCategories(categories);

        service.setComponentType(ComponentTypeEnum.SERVICE);
        Either<TopologyTemplate, StorageOperationStatus> createRes = topologyTemplateOperation.createTopologyTemplate(service);
        assertTrue(createRes.isLeft());

        Either<GraphVertex, TitanOperationStatus> getNodeTyeRes = titanDao.getVertexById(createRes.left().value().getUniqueId());
        assertTrue(getNodeTyeRes.isLeft());

        // serviceVertex = getNodeTyeRes.left().value();

        return getNodeTyeRes.left().value();
    }

    private <T extends ToscaDataDefinition> NodeType createNodeType(String nodeTypeName) {

        NodeType vf = new NodeType();
        String uniqueId =  RESOURCE_ID; // UniqueIdBuilder.buildResourceUniqueId();
        vf.setUniqueId(uniqueId);
        vf.setCreatorUserId((String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID));
        vf.getMetadata().put(JsonPresentationFields.NAME.getPresentation(), nodeTypeName);
        vf.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
        vf.getMetadata().put(JsonPresentationFields.VERSION.getPresentation(), "0.1");
        vf.getMetadata().put(JsonPresentationFields.TYPE.getPresentation(), ResourceTypeEnum.VF.name());
        vf.getMetadata().put(JsonPresentationFields.COMPONENT_TYPE.getPresentation(), ComponentTypeEnum.RESOURCE);
        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition cat = new CategoryDefinition();
        categories.add(cat);
        cat.setName(categoryName);
        List<SubCategoryDefinition> subCategories = new ArrayList<>();
        SubCategoryDefinition subCat = new SubCategoryDefinition();
        subCat.setName(subcategory);
        subCategories.add(subCat);
        cat.setSubcategories(subCategories);
        vf.setCategories(categories);

        List<String> derivedFrom = new ArrayList<>();
        derivedFrom.add("root");
        vf.setDerivedFrom(derivedFrom);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);

        Either<NodeType, StorageOperationStatus> createVFRes = nodeTypeOperation.createNodeType(vf);
        assertTrue(createVFRes.isLeft());

        Either<GraphVertex, TitanOperationStatus> getNodeTyeRes = titanDao.getVertexById(createVFRes.left().value().getUniqueId());
        assertTrue(getNodeTyeRes.isLeft());

        vfVertex = getNodeTyeRes.left().value();

        List<PropertyDataDefinition> addProperties = new ArrayList<>();
        PropertyDataDefinition prop11 = new PropertyDataDefinition();
        prop11.setName("prop11");
        prop11.setDefaultValue("def11");

        addProperties.add(prop11);

        PropertyDataDefinition prop22 = new PropertyDataDefinition();
        prop22.setName("prop22");
        prop22.setDefaultValue("def22");
        addProperties.add(prop22);

        StorageOperationStatus status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, addProperties, JsonPresentationFields.NAME);
        assertTrue(status == StorageOperationStatus.OK);

        PropertyDataDefinition prop33 = new PropertyDataDefinition();
        prop33.setName("prop33");
        prop33.setDefaultValue("def33");

        status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop33, JsonPresentationFields.NAME);
        assertTrue(status == StorageOperationStatus.OK);

        PropertyDataDefinition prop44 = new PropertyDataDefinition();
        prop44.setName("prop44");
        prop44.setDefaultValue("def44");

        status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop44, JsonPresentationFields.NAME);
        assertTrue(status == StorageOperationStatus.OK);

        PropertyDataDefinition capProp = new PropertyDataDefinition();
        capProp.setName("capProp");
        capProp.setDefaultValue("capPropDef");

        MapDataDefinition dataToCreate = new MapPropertiesDataDefinition();
        dataToCreate.put("capProp", capProp);

        Map<String, MapDataDefinition> capProps = new HashMap();
        capProps.put("capName", dataToCreate);

        Either<GraphVertex, StorageOperationStatus> res = nodeTypeOperation.associateElementToData(vfVertex, VertexTypeEnum.CAPABILITIES_PROPERTIES, EdgeLabelEnum.CAPABILITIES_PROPERTIES, capProps);

        List<String> pathKeys = new ArrayList<>();
        pathKeys.add("capName");
        capProp.setDefaultValue("BBBB");
        status = nodeTypeOperation.updateToscaDataDeepElementOfToscaElement(vfVertex, EdgeLabelEnum.CAPABILITIES_PROPERTIES, VertexTypeEnum.CAPABILITIES_PROPERTIES, capProp, pathKeys, JsonPresentationFields.NAME);
        return vf;
    }

    private GraphVertex createRootNodeType() {

        NodeType vf = new NodeType();
        String uniqueId = UniqueIdBuilder.buildResourceUniqueId();
        vf.setUniqueId(uniqueId);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);
        vf.setCreatorUserId((String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID));
        vf.getMetadata().put(JsonPresentationFields.NAME.getPresentation(), "root");
        vf.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
        vf.getMetadata().put(JsonPresentationFields.VERSION.getPresentation(), "1.0");
        vf.getMetadata().put(JsonPresentationFields.TYPE.getPresentation(), ResourceTypeEnum.VFC.name());
        vf.getMetadata().put(JsonPresentationFields.LIFECYCLE_STATE.getPresentation(), LifecycleStateEnum.CERTIFIED.name());
        vf.getMetadata().put(JsonPresentationFields.TOSCA_RESOURCE_NAME.getPresentation(), "root");
        vf.getMetadata().put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), true);

        List<CategoryDefinition> categories = new ArrayList<>();
        CategoryDefinition cat = new CategoryDefinition();
        categories.add(cat);
        cat.setName(categoryName);
        List<SubCategoryDefinition> subCategories = new ArrayList<>();
        SubCategoryDefinition subCat = new SubCategoryDefinition();
        subCat.setName(subcategory);
        subCategories.add(subCat);
        cat.setSubcategories(subCategories);
        vf.setCategories(categories);

        List<String> derivedFrom = new ArrayList<>();
        vf.setDerivedFrom(derivedFrom);

        Map<String, PropertyDataDefinition> properties = new HashMap<>();
        PropertyDataDefinition prop1 = new PropertyDataDefinition();
        prop1.setName("derived1");
        prop1.setDefaultValue("deriveddef1");

        properties.put("derived1", prop1);

        PropertyDataDefinition prop2 = new PropertyDataDefinition();
        prop2.setUniqueId("derived2");
        prop2.setName("deriveddef2");
        properties.put("derived2", prop2);

        PropertyDataDefinition prop3 = new PropertyDataDefinition();
        prop3.setName("derived3");
        prop3.setDefaultValue("deriveddef3");
        properties.put("derived3", prop3);

        vf.setProperties(properties);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);
        Either<NodeType, StorageOperationStatus> createVFRes = nodeTypeOperation.createNodeType(vf);
        assertTrue(createVFRes.isLeft());

        Either<GraphVertex, TitanOperationStatus> getNodeTyeRes = titanDao.getVertexById(createVFRes.left().value().getUniqueId());
        assertTrue(getNodeTyeRes.isLeft());
        return getNodeTyeRes.left().value();
    }

    private void createUsers() {

        GraphVertex ownerV = new GraphVertex(VertexTypeEnum.USER);
        ownerV.setUniqueId("user1");

        Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
        metadataProperties.put(GraphPropertyEnum.USERID, ownerV.getUniqueId());
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.USER.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, "user1");
        ownerV.setMetadataProperties(metadataProperties);
        ownerV.updateMetadataJsonWithCurrentMetadataProperties();
        ownerV.setJson(new HashMap<>());
        Either<GraphVertex, TitanOperationStatus> createUserRes = titanDao.createVertex(ownerV);
        assertTrue(createUserRes.isLeft());

        ownerVertex = createUserRes.left().value();

        GraphVertex modifierV = new GraphVertex(VertexTypeEnum.USER);
        modifierV.setUniqueId("user2");

        metadataProperties = new HashMap<>();
        metadataProperties.put(GraphPropertyEnum.USERID, modifierV.getUniqueId());
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.USER.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, "user2");
        modifierV.setMetadataProperties(metadataProperties);
        modifierV.updateMetadataJsonWithCurrentMetadataProperties();
        modifierV.setJson(new HashMap<>());
        createUserRes = titanDao.createVertex(modifierV);
        assertTrue(createUserRes.isLeft());

        modifierVertex = createUserRes.left().value();

        Either<GraphVertex, TitanOperationStatus> getOwnerRes = lifecycleOperation.findUser(ownerVertex.getUniqueId());
        assertTrue(getOwnerRes.isLeft());

    }

    @After
    public void teardown() {
        clearGraph();
    }

    private void clearGraph() {
        Either<TitanGraph, TitanOperationStatus> graphResult = titanDao.getGraph();
        TitanGraph graph = graphResult.left().value();

        Iterable<TitanVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<TitanVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                TitanVertex vertex = iterator.next();
                vertex.remove();
            }
        }
        titanDao.commit();
    }

}
