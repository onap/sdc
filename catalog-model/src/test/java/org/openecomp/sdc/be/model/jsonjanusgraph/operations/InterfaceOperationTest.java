/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = "classpath:application-context-test.xml")
public class InterfaceOperationTest extends ModelTestBase {

    private static final String RESOURCE_NAME = "Resource Name";
    private static final String RESOURCE_ID = "resourceID";
    private static final String SERVICE_NAME = "Service Name";
    private static final String SERVICE_ID = "serviceID";
    private final String categoryName = "category";
    private final String subcategory = "mycategory";
    private final Service service = createService();
    private final org.openecomp.sdc.be.model.Resource resource = createResource();
    @Resource
    protected JanusGraphDao janusGraphDao;
    @Resource
    protected NodeTypeOperation nodeTypeOperation;
    @Resource
    protected TopologyTemplateOperation topologyTemplateOperation;
    @Resource
    private InterfaceOperation interfaceOperation;
    @Resource
    private ToscaElementLifecycleOperation lifecycleOperation;
    private GraphVertex ownerVertex;

    @BeforeAll
    public static void initInterfacesOperation() {
        init();
    }

    @BeforeEach
    public void setupBefore() {
        GraphTestUtils.clearGraph(janusGraphDao);
        createUsers();
        createResourceCategory();
        createServiceCategory();
        GraphTestUtils.createRootCatalogVertex(janusGraphDao);
        createRootNodeType();
        createNodeType("resource", RESOURCE_ID);
        createNodeType("service", SERVICE_ID);
        createTopologyTemplate("firstService");
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
        Either<GraphVertex, JanusGraphOperationStatus> createUserRes = janusGraphDao.createVertex(ownerV);

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
        createUserRes = janusGraphDao.createVertex(modifierV);
        createUserRes.left().value();

        lifecycleOperation.findUser(ownerVertex.getUniqueId());
    }

    private void createResourceCategory() {
        GraphVertex cat = new GraphVertex(VertexTypeEnum.RESOURCE_CATEGORY);
        Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
        String catId = UniqueIdBuilder.buildComponentCategoryUid(categoryName, VertexTypeEnum.RESOURCE_CATEGORY);
        cat.setUniqueId(catId);
        metadataProperties.put(GraphPropertyEnum.UNIQUE_ID, catId);
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.RESOURCE_CATEGORY.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, categoryName);
        metadataProperties
            .put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
        cat.setMetadataProperties(metadataProperties);
        cat.updateMetadataJsonWithCurrentMetadataProperties();

        GraphVertex subCat = new GraphVertex(VertexTypeEnum.RESOURCE_SUBCATEGORY);
        metadataProperties = new HashMap<>();
        String subCatId = UniqueIdBuilder.buildSubCategoryUid(cat.getUniqueId(), subcategory);
        subCat.setUniqueId(subCatId);
        metadataProperties.put(GraphPropertyEnum.UNIQUE_ID, subCatId);
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.RESOURCE_SUBCATEGORY.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, subcategory);
        metadataProperties
            .put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(subcategory));
        subCat.setMetadataProperties(metadataProperties);
        subCat.updateMetadataJsonWithCurrentMetadataProperties();

        Either<GraphVertex, JanusGraphOperationStatus> catRes = janusGraphDao.createVertex(cat);
        Either<GraphVertex, JanusGraphOperationStatus> subCatRes = janusGraphDao.createVertex(subCat);
        janusGraphDao.createEdge(catRes.left().value().getVertex(), subCatRes.left().value().getVertex(),
            EdgeLabelEnum.SUB_CATEGORY, new HashMap<>());
    }

    private void createServiceCategory() {
        GraphVertex cat = new GraphVertex(VertexTypeEnum.SERVICE_CATEGORY);
        Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
        String catId = UniqueIdBuilder.buildComponentCategoryUid(categoryName, VertexTypeEnum.SERVICE_CATEGORY);
        cat.setUniqueId(catId);
        metadataProperties.put(GraphPropertyEnum.UNIQUE_ID, catId);
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.SERVICE_CATEGORY.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, categoryName);
        metadataProperties
            .put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
        cat.setMetadataProperties(metadataProperties);
        cat.updateMetadataJsonWithCurrentMetadataProperties();
        janusGraphDao.createVertex(cat);
    }

    private void createRootNodeType() {
        NodeType vf = new NodeType();
        String uniqueId = UniqueIdBuilder.buildResourceUniqueId();
        vf.setUniqueId(uniqueId);
        vf.setComponentType(ComponentTypeEnum.RESOURCE);
        vf.setCreatorUserId((String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID));
        vf.getMetadata().put(JsonPresentationFields.NAME.getPresentation(), "root");
        vf.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
        vf.getMetadata().put(JsonPresentationFields.VERSION.getPresentation(), "1.0");
        vf.getMetadata().put(JsonPresentationFields.TYPE.getPresentation(), ResourceTypeEnum.VFC.name());
        vf.getMetadata()
            .put(JsonPresentationFields.LIFECYCLE_STATE.getPresentation(), LifecycleStateEnum.CERTIFIED.name());
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

        Either<GraphVertex, JanusGraphOperationStatus> getNodeTyeRes =
            janusGraphDao.getVertexById(createVFRes.left().value().getUniqueId());
        getNodeTyeRes.left().value();
    }

    private <T extends ToscaDataDefinition> void createNodeType(String nodeTypeName, String uniqueId) {
        NodeType vf = new NodeType();
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

        List<PropertyDataDefinition> addProperties = new ArrayList<>();
        PropertyDataDefinition prop11 = new PropertyDataDefinition();
        prop11.setName("prop11");
        prop11.setDefaultValue("def11");

        addProperties.add(prop11);

        PropertyDataDefinition prop22 = new PropertyDataDefinition();
        prop22.setName("prop22");
        prop22.setDefaultValue("def22");
        addProperties.add(prop22);

        Either<NodeType, StorageOperationStatus> createVFRes = nodeTypeOperation.createNodeType(vf);
        Either<GraphVertex, JanusGraphOperationStatus> getNodeTyeRes =
            janusGraphDao.getVertexById(createVFRes.left().value().getUniqueId());
        GraphVertex vfVertex = getNodeTyeRes.left().value();
        StorageOperationStatus status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex, EdgeLabelEnum.PROPERTIES,
            VertexTypeEnum.PROPERTIES, addProperties, JsonPresentationFields.NAME);
        assertSame(StorageOperationStatus.OK, status);

        PropertyDataDefinition prop33 = new PropertyDataDefinition();
        prop33.setName("prop33");
        prop33.setDefaultValue("def33");

        status = nodeTypeOperation
            .addToscaDataToToscaElement(vfVertex, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES,
                prop33, JsonPresentationFields.NAME);
        assertSame(StorageOperationStatus.OK, status);

        PropertyDataDefinition prop44 = new PropertyDataDefinition();
        prop44.setName("prop44");
        prop44.setDefaultValue("def44");

        status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex.getUniqueId(), EdgeLabelEnum.PROPERTIES,
            VertexTypeEnum.PROPERTIES, prop44, JsonPresentationFields.NAME);
        assertSame(StorageOperationStatus.OK, status);

        PropertyDataDefinition capProp = new PropertyDataDefinition();
        capProp.setName("capProp");
        capProp.setDefaultValue("capPropDef");

        MapDataDefinition dataToCreate = new MapPropertiesDataDefinition();
        dataToCreate.put("capProp", capProp);

        Map<String, MapDataDefinition> capProps = new HashMap<>();
        capProps.put("capName", dataToCreate);

        nodeTypeOperation.associateElementToData(vfVertex, VertexTypeEnum.CAPABILITIES_PROPERTIES,
            EdgeLabelEnum.CAPABILITIES_PROPERTIES, capProps);

        List<String> pathKeys = new ArrayList<>();
        pathKeys.add("capName");
        capProp.setDefaultValue("BBBB");
        nodeTypeOperation.updateToscaDataDeepElementOfToscaElement(vfVertex, EdgeLabelEnum.CAPABILITIES_PROPERTIES,
            VertexTypeEnum.CAPABILITIES_PROPERTIES, capProp, pathKeys, JsonPresentationFields.NAME);
    }

    private void createTopologyTemplate(String name) {
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
        Either<TopologyTemplate, StorageOperationStatus> createRes =
            topologyTemplateOperation.createTopologyTemplate(service);
        Either<GraphVertex, JanusGraphOperationStatus> getNodeTyeRes =
            janusGraphDao.getVertexById(createRes.left().value().getUniqueId());

        getNodeTyeRes.left().value();
    }

    @AfterEach
    public void cleanAfter() {
        GraphTestUtils.clearGraph(janusGraphDao);
    }

    @Test
    public void testAddInterface_Service() {
        testAddSingleInterface(service);
    }

    private void testAddSingleInterface(Component component) {
        InterfaceDefinition interfaceDefinition = buildInterfaceDefinition("1");
        final var res = interfaceOperation.addInterfaces(component, Collections.singletonList(interfaceDefinition));
        Assert.assertTrue(res.isLeft());
        Assert.assertEquals("1", res.left().value().get(0).getUniqueId());
    }

    private InterfaceDefinition buildInterfaceDefinition(String uniqueId) {
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.standard");
        interfaceDefinition.setUniqueId(uniqueId);
        interfaceDefinition.setOperationsMap(createMockOperationMap());
        return interfaceDefinition;
    }

    private Map<String, Operation> createMockOperationMap() {
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put("op1", createMockOperation());
        return operationMap;
    }

    private Operation createMockOperation() {
        Operation operation = new Operation();
        operation.setDefinition(false);
        operation.setName("create");
        operation.setUniqueId("op1");
        return operation;
    }

    @Test
    public void testAddInterface_Resource() {
        testAddMultipleInterface(resource);
    }

    private void testAddMultipleInterface(Component component) {
        InterfaceDefinition interfaceDefinition1 = buildInterfaceDefinition("1");
        InterfaceDefinition interfaceDefinition2 = buildInterfaceDefinition("2");
        List<InterfaceDefinition> interfaceDefinitions = new ArrayList<>();
        interfaceDefinitions.add(interfaceDefinition1);
        interfaceDefinitions.add(interfaceDefinition2);
        final var res = interfaceOperation.addInterfaces(component, interfaceDefinitions);
        Assert.assertTrue(res.isLeft());
        Assert.assertEquals(2, res.left().value().size());
    }

    @Test
    public void testUpdateInterface_Service() {
        testUpdateInterface(service);
    }

    private void testUpdateInterface(Component component) {
        InterfaceDefinition interfaceDefinition = buildInterfaceDefinition("1");
        var res = interfaceOperation.addInterfaces(component, Collections.singletonList(interfaceDefinition));
        Assert.assertTrue(res.isLeft());
        List<InterfaceDefinition> value = res.left().value();
        InterfaceDefinition createdInterfaceDef = value.get(0);
        String newDescription = "New Description";
        createdInterfaceDef.setDescription(newDescription);
        res = interfaceOperation.updateInterfaces(component, Collections.singletonList(createdInterfaceDef));
        assertTrue(res.isLeft());
        assertEquals(newDescription, res.left().value().get(0).getDescription());
    }

    @Test
    public void testUpdateInterface_Resource() {
        testUpdateInterface(resource);
    }

    @Test
    public void testDeleteInterface_Service() {
        testDeleteInterface(service);
    }

    private void testDeleteInterface(Component component) {
        InterfaceDefinition interfaceDefinition = buildInterfaceDefinition("1");
        var res = interfaceOperation.addInterfaces(component, Collections.singletonList(interfaceDefinition));
        Assert.assertTrue(res.isLeft());
        List<InterfaceDefinition> value = res.left().value();
        final var deleteInterfaceOperationRes = interfaceOperation.deleteInterface(component, value.get(0).getUniqueId());
        assertTrue(deleteInterfaceOperationRes.isLeft());
    }

    @Test
    public void testDeleteInterface_Resource() {
        testDeleteInterface(resource);
    }

    @Test
    public void testUpdateInterfaceShouldFailWhenNOtCreatedFirst() {
        Component component = createResource();
        InterfaceDefinition interfaceDefinition = buildInterfaceDefinitionWithoutOperation();
        interfaceDefinition.setOperationsMap(createMockOperationMap());
        final var res = interfaceOperation.updateInterfaces(component, Collections.singletonList(interfaceDefinition));
        Assert.assertTrue(res.isRight());
    }

    private InterfaceDefinition buildInterfaceDefinitionWithoutOperation() {
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType("tosca.interfaces.standard");
        return interfaceDefinition;
    }

    private org.openecomp.sdc.be.model.Resource createResource() {
        org.openecomp.sdc.be.model.Resource resource = new org.openecomp.sdc.be.model.Resource();
        resource.setUniqueId(RESOURCE_ID);
        resource.setName(RESOURCE_NAME);
        resource.setDescription("My short description");
        resource.setInterfaces(createMockInterfaceDefinition());
        return resource;
    }

    private Service createService() {
        Service service = new Service();
        service.setUniqueId(SERVICE_ID);
        service.setName(SERVICE_NAME);
        service.setDescription("My short description");
        service.setInterfaces(createMockInterfaceDefinition());
        return service;
    }

    private Map<String, InterfaceDefinition> createMockInterfaceDefinition() {
        Map<String, Operation> operationMap = createMockOperationMap();
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put("int1", createInterface("int1", "Interface 1", "lifecycle", "tosca", operationMap));
        return interfaceDefinitionMap;
    }

    private InterfaceDefinition createInterface(String uniqueId, String description, String type,
                                                String toscaResourceName, Map<String, Operation> op) {
        InterfaceDefinition id = new InterfaceDefinition();
        id.setType(type);
        id.setDescription(description);
        id.setUniqueId(uniqueId);
        id.setToscaResourceName(toscaResourceName);
        id.setOperationsMap(op);
        return id;
    }

    @AfterEach
    public void teardown() {
        GraphTestUtils.clearGraph(janusGraphDao);
    }

}
