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
 */

package org.openecomp.sdc.be.model.operations.impl;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ToscaElementLifecycleOperationTest extends ModelTestBase {

    @javax.annotation.Resource
    protected JanusGraphDao janusGraphDao;

    @javax.annotation.Resource
    private NodeTypeOperation nodeTypeOperation;

    @javax.annotation.Resource
    private TopologyTemplateOperation topologyTemplateOperation;

    @javax.annotation.Resource
    private ToscaElementLifecycleOperation lifecycleOperation;

    String categoryName = "category";
    String subcategory = "mycategory";
    String outputDirectory = "C:\\Output";

    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void initLifecycleOperation() {
        ModelTestBase.init();
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
        GraphTestUtils.createRootCatalogVertex(janusGraphDao);
        rootVertex = createRootNodeType();
        createNodeType("firstVf");
        serviceVertex = createTopologyTemplate("firstService");
    }

    @Test
    public void lifecycleTest() {
        Either<ToscaElement, StorageOperationStatus> res = lifecycleOperation.checkinToscaELement(LifecycleStateEnum.findState((String) vfVertex.getMetadataProperty(GraphPropertyEnum.STATE)), vfVertex.getUniqueId(), modifierVertex.getUniqueId(),
                ownerVertex.getUniqueId());
        StorageOperationStatus status;

        assertTrue(res.isLeft());
        // 1-node type
        // 2-vf
        // 3- service
        verifyInCatalogData(3, null);

        String id = res.left().value().getUniqueId();

        res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), modifierVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        verifyInCatalogData(3, null);

        PropertyDataDefinition prop55 = new PropertyDataDefinition();
        prop55.setName("prop55");
        prop55.setDefaultValue("def55");

        status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop55, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        CapabilityDataDefinition cap1 = new CapabilityDataDefinition();
        cap1.setName("cap1");
        cap1.setDescription("create");
        cap1.setUniqueId(UniqueIdBuilder.buildCapabilityUid(id, "cap1"));

        status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.CAPABILITIES, VertexTypeEnum.CAPABILITIES, cap1, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        res = lifecycleOperation.checkinToscaELement(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        prop55.setDefaultValue("AAAAAAAA");
        status = nodeTypeOperation.updateToscaDataOfToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop55, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        cap1.setDescription("update");

        status = nodeTypeOperation.updateToscaDataOfToscaElement(id, EdgeLabelEnum.CAPABILITIES, VertexTypeEnum.CAPABILITIES, cap1, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        PropertyDataDefinition prop66 = new PropertyDataDefinition();
        prop66.setName("prop66");
        prop66.setDefaultValue("def66");

        status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop66, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        res = lifecycleOperation.requestCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.certifyToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        verifyInCatalogData(3, null);

        res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), modifierVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        verifyInCatalogData(4, null);

        PropertyDataDefinition prop77 = new PropertyDataDefinition();
        prop77.setName("prop77");
        prop77.setDefaultValue("def77");

        status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop77, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        res = lifecycleOperation.checkinToscaELement(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        PropertyDataDefinition prop88 = new PropertyDataDefinition();
        prop88.setName("prop88");
        prop88.setDefaultValue("def88");

        status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop88, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        res = lifecycleOperation.requestCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.certifyToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();
        verifyInCatalogData(3, null);

        res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        verifyInCatalogData(4, null);

        PropertyDataDefinition prop99 = new PropertyDataDefinition();
        prop99.setName("prop99");
        prop99.setDefaultValue("def99");

        status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop99, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        res = lifecycleOperation.requestCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        status = nodeTypeOperation.deleteToscaDataElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, "prop99", JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        // cancel certification
        res = lifecycleOperation.cancelOrFailCertification(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();

        // fail certification
        res = lifecycleOperation.cancelOrFailCertification(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(res.isLeft());
        id = res.left().value().getUniqueId();
        verifyInCatalogData(4, null);
        // exportGraphMl(janusGraphDao.getGraph().left().value());

    }

    @Test
    public void serviceConformanceLevelTest() {
        Either<ToscaElement, StorageOperationStatus> res = lifecycleOperation.checkinToscaELement(LifecycleStateEnum.findState((String) serviceVertex.getMetadataProperty(GraphPropertyEnum.STATE)), serviceVertex.getUniqueId(),
                modifierVertex.getUniqueId(), ownerVertex.getUniqueId());

        assertTrue(res.isLeft());
        String id = res.left().value().getUniqueId();

        res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), modifierVertex.getUniqueId());
        assertTrue(res.isLeft());

        String conformanceLevel = res.left().value().getMetadataValue(JsonPresentationFields.CONFORMANCE_LEVEL).toString();
        assertEquals(conformanceLevel, ModelTestBase.configurationManager.getConfiguration().getToscaConformanceLevel());
    }

    @Test
    public void catalogTest() {
        // start position - 3 in catalog
        List<String> expectedIds = new ArrayList<>();
        expectedIds.add(rootVertex.getUniqueId());
        expectedIds.add(vfVertex.getUniqueId());
        expectedIds.add(serviceVertex.getUniqueId());

        verifyInCatalogData(3, expectedIds);

        GraphVertex vertex4 = createTopologyTemplate("topTemp4");
        expectedIds.add(vertex4.getUniqueId());
        verifyInCatalogData(4, expectedIds);

        Either<ToscaElement, StorageOperationStatus> res = lifecycleOperation.undoCheckout(vertex4.getUniqueId());
        expectedIds.remove(vertex4.getUniqueId());
        verifyInCatalogData(3, expectedIds);

        vertex4 = createTopologyTemplate("topTemp4");
        expectedIds.add(vertex4.getUniqueId());
        verifyInCatalogData(4, expectedIds);

        res = lifecycleOperation.checkinToscaELement(LifecycleStateEnum.findState((String) vertex4.getMetadataProperty(GraphPropertyEnum.STATE)), vertex4.getUniqueId(), modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        Either<ToscaElement, StorageOperationStatus> certifyToscaElement = lifecycleOperation.certifyToscaElement(vertex4.getUniqueId(), modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(certifyToscaElement.isLeft());
        expectedIds.remove(vertex4.getUniqueId());
        String certifiedId = certifyToscaElement.left().value().getUniqueId();
        expectedIds.add(certifiedId);
        verifyInCatalogData(4, expectedIds);

        res = lifecycleOperation.checkoutToscaElement(certifiedId, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
        assertTrue(certifyToscaElement.isLeft());
        expectedIds.add(res.left().value().getUniqueId());
        verifyInCatalogData(5, expectedIds);
    }

    @Test
    public void testGetToscaElOwner_Fail(){
        Either<User, StorageOperationStatus> result;
        String toscaEleId = "toscaElementId";
        janusGraphDao.getVertexById(toscaEleId, JsonParseFlagEnum.NoParse);
        result = lifecycleOperation.getToscaElementOwner(toscaEleId);
        assertEquals(StorageOperationStatus.NOT_FOUND, result.right().value());
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

        Either<GraphVertex, JanusGraphOperationStatus> catRes = janusGraphDao.createVertex(cat);

        Either<GraphVertex, JanusGraphOperationStatus> subCatRes = janusGraphDao.createVertex(subCat);

        JanusGraphOperationStatus
            status = janusGraphDao
            .createEdge(catRes.left().value().getVertex(), subCatRes.left().value().getVertex(), EdgeLabelEnum.SUB_CATEGORY, new HashMap<>());
        assertEquals(JanusGraphOperationStatus.OK, status);
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

        Either<GraphVertex, JanusGraphOperationStatus> catRes = janusGraphDao.createVertex(cat);

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

        Either<GraphVertex, JanusGraphOperationStatus> getNodeTyeRes = janusGraphDao
            .getVertexById(createRes.left().value().getUniqueId());
        assertTrue(getNodeTyeRes.isLeft());

        // serviceVertex = getNodeTyeRes.left().value();

        return getNodeTyeRes.left().value();
    }

    private <T extends ToscaDataDefinition> NodeType createNodeType(String nodeTypeName) {

        NodeType vf = new NodeType();
        String uniqueId = UniqueIdBuilder.buildResourceUniqueId();
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

        Either<GraphVertex, JanusGraphOperationStatus> getNodeTyeRes = janusGraphDao
            .getVertexById(createVFRes.left().value().getUniqueId());
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
        assertSame(status, StorageOperationStatus.OK);

        PropertyDataDefinition prop33 = new PropertyDataDefinition();
        prop33.setName("prop33");
        prop33.setDefaultValue("def33");

        status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop33, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        PropertyDataDefinition prop44 = new PropertyDataDefinition();
        prop44.setName("prop44");
        prop44.setDefaultValue("def44");

        status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop44, JsonPresentationFields.NAME);
        assertSame(status, StorageOperationStatus.OK);

        PropertyDataDefinition capProp = new PropertyDataDefinition();
        capProp.setName("capProp");
        capProp.setDefaultValue("capPropDef");

        MapDataDefinition dataToCreate = new MapPropertiesDataDefinition();
        dataToCreate.put("capProp", capProp);

        Map<String, MapDataDefinition> capProps = new HashMap();
        capProps.put("capName", dataToCreate);

        Either<GraphVertex, StorageOperationStatus> res = nodeTypeOperation.associateElementToData(vfVertex, VertexTypeEnum.CAPABILITIES_PROPERTIES, EdgeLabelEnum.CAPABILITIES_PROPERTIES, capProps);

        // exportGraphMl(janusGraphDao.getGraph().left().value());

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

        Either<GraphVertex, JanusGraphOperationStatus> getNodeTyeRes = janusGraphDao
            .getVertexById(createVFRes.left().value().getUniqueId());
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
        Either<GraphVertex, JanusGraphOperationStatus> createUserRes = janusGraphDao.createVertex(ownerV);
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
        createUserRes = janusGraphDao.createVertex(modifierV);
        assertTrue(createUserRes.isLeft());

        modifierVertex = createUserRes.left().value();

        Either<GraphVertex, JanusGraphOperationStatus> getOwnerRes = lifecycleOperation.findUser(ownerVertex.getUniqueId());
        assertTrue(getOwnerRes.isLeft());

    }

    public void verifyInCatalogData(int expected, List<String> expectedIds) {

        Either<List<CatalogComponent>, StorageOperationStatus> highestResourcesRes = topologyTemplateOperation.getElementCatalogData(true, null);
        assertTrue(highestResourcesRes.isLeft());
        List<CatalogComponent> highestResources = highestResourcesRes.left().value();
        // calculate expected count value
        assertEquals(expected, highestResources.stream().count());
        if (expectedIds != null) {
            highestResources.forEach(a -> assertTrue(expectedIds.contains(a.getUniqueId())));
        }
    }

    @After
    public void teardown() {
        clearGraph();
    }

    private void clearGraph() {
        Either<JanusGraph, JanusGraphOperationStatus> graphResult = janusGraphDao.getGraph();
        JanusGraph graph = graphResult.left().value();

        Iterable<JanusGraphVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<JanusGraphVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                JanusGraphVertex vertex = iterator.next();
                vertex.remove();
            }
        }
        janusGraphDao.commit();
    }
}
