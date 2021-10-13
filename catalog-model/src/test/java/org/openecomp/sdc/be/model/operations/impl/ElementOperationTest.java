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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.CategoryBaseTypeConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.BaseType;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:application-context-test.xml")
public class ElementOperationTest extends ModelTestBase {

    @InjectMocks
    private ElementOperation elementOperation;

    @Mock
    private JanusGraphGenericDao janusGraphDao;

    private static String CATEGORY = "category";
    private static String SUBCATEGORY = "subcategory";

    @BeforeAll
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    void beforeEachInit() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetArtifactsTypes() {
        final List<ArtifactConfiguration> expectedArtifactConfigurationList = new ArrayList<>();
        final ArtifactConfiguration artifactConfiguration1 = new ArtifactConfiguration();
        artifactConfiguration1.setType("type1");
        expectedArtifactConfigurationList.add(artifactConfiguration1);
        final ArtifactConfiguration artifactConfiguration2 = new ArtifactConfiguration();
        artifactConfiguration2.setType("type2");
        expectedArtifactConfigurationList.add(artifactConfiguration2);
        final ArtifactConfiguration artifactConfiguration3 = new ArtifactConfiguration();
        artifactConfiguration3.setType("type3");
        expectedArtifactConfigurationList.add(artifactConfiguration3);
        configurationManager.getConfiguration().setArtifacts(expectedArtifactConfigurationList);

        List<ArtifactType> actualArtifactTypes = elementOperation.getAllArtifactTypes();
        assertNotNull(actualArtifactTypes);
        assertEquals(expectedArtifactConfigurationList.size(), actualArtifactTypes.size());
        boolean allMatch = actualArtifactTypes.stream().allMatch(artifactType ->
            expectedArtifactConfigurationList.stream()
                .anyMatch(artifactConfiguration -> artifactConfiguration.getType().equals(artifactType.getName()))
        );
        assertTrue(allMatch);

        expectedArtifactConfigurationList.remove(0);
        actualArtifactTypes = elementOperation.getAllArtifactTypes();
        assertNotNull(actualArtifactTypes);
        assertEquals(expectedArtifactConfigurationList.size(), actualArtifactTypes.size());

        allMatch = actualArtifactTypes.stream().allMatch(artifactType ->
            expectedArtifactConfigurationList.stream()
                .anyMatch(artifactConfiguration -> artifactConfiguration.getType().equals(artifactType.getName()))
        );
        assertTrue(allMatch);
    }

    // @Test
    public void testGetResourceAndServiceCategoty() {
        String id = OperationTestsUtil.deleteAndCreateResourceCategory(CATEGORY, SUBCATEGORY, janusGraphDao);

        Either<CategoryDefinition, ActionStatus> res = elementOperation.getCategory(NodeTypeEnum.ResourceNewCategory, id);
        assertTrue(res.isLeft());
        CategoryDefinition categoryDefinition = (CategoryDefinition) res.left().value();
        assertEquals(CATEGORY, categoryDefinition.getName());
        assertEquals(SUBCATEGORY, categoryDefinition.getSubcategories().get(0).getName());

        id = OperationTestsUtil.deleteAndCreateServiceCategory(CATEGORY, janusGraphDao);

        res = elementOperation.getCategory(NodeTypeEnum.ServiceNewCategory, id);
        assertTrue(res.isLeft());
        categoryDefinition = (CategoryDefinition) res.left().value();
        assertEquals(CATEGORY, categoryDefinition.getName());
    }

    private ElementOperation createTestSubject() {
        return new ElementOperation(new JanusGraphGenericDao(new JanusGraphClient()),
            new HealingJanusGraphDao(new HealingPipelineDao(), new JanusGraphClient()));
    }

    @Test
    public void testGetAllServiceCategories() throws Exception {
        ElementOperation testSubject;
        Either<List<CategoryDefinition>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getAllServiceCategories();
    }

    @Test
    public void testGetAllResourceCategories() throws Exception {
        ElementOperation testSubject;
        Either<List<CategoryDefinition>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getAllResourceCategories();
    }

    @Test
    public void testGetAllProductCategories() throws Exception {
        ElementOperation testSubject;
        Either<List<CategoryDefinition>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getAllProductCategories();
    }

    @Test
    public void testCreateCategory() throws Exception {
        ElementOperation testSubject;
        CategoryDefinition category = new CategoryDefinition();
        NodeTypeEnum nodeType = NodeTypeEnum.AdditionalInfoParameters;
        Either<CategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createCategory(category, nodeType);
    }

    @Test
    public void testCreateCategory_1() throws Exception {
        ElementOperation testSubject;
        CategoryDefinition category = new CategoryDefinition();
        NodeTypeEnum nodeType = NodeTypeEnum.ArtifactRef;
        boolean inTransaction = false;
        Either<CategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createCategory(category, nodeType, inTransaction);
    }

    @Test
    public void testCreateSubCategory() throws Exception {
        ElementOperation testSubject;
        String categoryId = "";
        SubCategoryDefinition subCategory = null;
        NodeTypeEnum nodeType = null;
        Either<SubCategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createSubCategory(categoryId, subCategory, nodeType);
    }

    @Test
    public void testCreateSubCategory_1() throws Exception {
        ElementOperation testSubject;
        String categoryId = "";
        SubCategoryDefinition subCategory = null;
        NodeTypeEnum nodeType = null;
        boolean inTransaction = false;
        Either<SubCategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createSubCategory(categoryId, subCategory, nodeType, inTransaction);
    }

    @Test
    public void testCreateGrouping() throws Exception {
        ElementOperation testSubject;
        String subCategoryId = "";
        GroupingDefinition grouping = null;
        NodeTypeEnum nodeType = null;
        Either<GroupingDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createGrouping(subCategoryId, grouping, nodeType);
    }

    @Test
    public void testGetAllCategories() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = NodeTypeEnum.Capability;
        boolean inTransaction = false;
        Either<List<CategoryDefinition>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getAllCategories(nodeType, inTransaction);
    }

    @Test
    public void testGetCategory() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = NodeTypeEnum.CapabilityType;
        String categoryId = "";
        Either<CategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getCategory(nodeType, categoryId);
    }

    @Test
    public void testGetSubCategory() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = NodeTypeEnum.Group;
        String subCategoryId = "";
        Either<SubCategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getSubCategory(nodeType, subCategoryId);
    }

    @Test
    public void testDeleteCategory() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = NodeTypeEnum.getByName("resource");
        String categoryId = "";
        Either<CategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.deleteCategory(nodeType, categoryId);
    }

    @Test
    public void testDeleteSubCategory() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = NodeTypeEnum.Attribute;
        String subCategoryId = "";
        Either<SubCategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.deleteSubCategory(nodeType, subCategoryId);
    }

    @Test
    public void testDeleteGrouping() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = NodeTypeEnum.DataType;
        String groupingId = "";
        Either<GroupingDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.deleteGrouping(nodeType, groupingId);
    }

    @Test
    public void testIsCategoryUniqueForType() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = null;
        String normalizedName = "";
        Either<Boolean, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isCategoryUniqueForType(nodeType, normalizedName);
    }

    @Test
    public void testIsSubCategoryUniqueForCategory() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = null;
        String subCategoryNormName = "";
        String parentCategoryId = "";
        Either<Boolean, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isSubCategoryUniqueForCategory(nodeType, subCategoryNormName, parentCategoryId);
    }

    @Test
    public void testIsGroupingUniqueForSubCategory() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = null;
        String groupingNormName = "";
        String parentSubCategoryId = "";
        Either<Boolean, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.isGroupingUniqueForSubCategory(nodeType, groupingNormName, parentSubCategoryId);
    }

    @Test
    public void testGetSubCategoryUniqueForType() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = null;
        String normalizedName = "";
        Either<SubCategoryDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getSubCategoryUniqueForType(nodeType, normalizedName);
    }

    @Test
    public void testGetGroupingUniqueForType() throws Exception {
        ElementOperation testSubject;
        NodeTypeEnum nodeType = null;
        String groupingNormalizedName = "";
        Either<GroupingDefinition, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getGroupingUniqueForType(nodeType, groupingNormalizedName);
    }

    @Test
    public void testGetAllTags() throws Exception {
        ElementOperation testSubject;
        Either<List<Tag>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getAllTags();
    }

    @Test
    public void testGetCategoryData() throws Exception {
        ElementOperation testSubject;
        String name = "";
        NodeTypeEnum type = NodeTypeEnum.DataType;
        Class<T> clazz = null;
        Either<org.openecomp.sdc.be.resources.data.CategoryData, StorageOperationStatus> result;

        // test 1
        testSubject = createTestSubject();
        name = null;
        result = testSubject.getCategoryData(name, type, null);

        // test 2
        testSubject = createTestSubject();
        name = "";
        result = testSubject.getCategoryData(name, type, null);
    }

    @Test
    public void testGetAllPropertyScopes() throws Exception {
        ElementOperation testSubject;
        Either<List<PropertyScope>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getAllPropertyScopes();
    }

    @Test
    public void testGetResourceTypesMap() throws Exception {
        ElementOperation testSubject;
        Either<Map<String, String>, ActionStatus> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getResourceTypesMap();
    }

    @Test
    public void testGetNewCategoryData() throws Exception {
        ElementOperation testSubject;
        String name = "";
        NodeTypeEnum type = NodeTypeEnum.HeatParameter;
        Class<T> clazz = null;
        Either<CategoryData, StorageOperationStatus> result;

        // test 1
        testSubject = createTestSubject();
        name = null;
        result = testSubject.getNewCategoryData(name, type, null);

        // test 2
        testSubject = createTestSubject();
        name = "";
        result = testSubject.getNewCategoryData(name, type, null);
    }

    @Test
    public void testBaseTypes_serviceSpecific() {
        Map<String, CategoryBaseTypeConfig> preExistingServiceNodeTypes = configurationManager.getConfiguration().getServiceBaseNodeTypes();
        Map<String, String> preExistingGenericNodeTypes = configurationManager.getConfiguration().getGenericAssetNodeTypes();

        try {
            final Map<String, CategoryBaseTypeConfig> serviceBaseNodeTypeConfigMap = new HashMap<>();
            final var categoryBaseTypeConfig = new CategoryBaseTypeConfig();
            categoryBaseTypeConfig.setBaseTypes(List.of("org.base.type"));
            serviceBaseNodeTypeConfigMap.put("serviceCategoryA", categoryBaseTypeConfig);
            configurationManager.getConfiguration().setServiceBaseNodeTypes(serviceBaseNodeTypeConfigMap);

            Map<String, String> genericNodeTypes = new HashMap<>();
            genericNodeTypes.put("service", "org.service.default");
            configurationManager.getConfiguration().setGenericAssetNodeTypes(genericNodeTypes);

            HealingJanusGraphDao healingJanusGraphDao = mock(HealingJanusGraphDao.class);
            ElementOperation elementOperation = new ElementOperation(new JanusGraphGenericDao(new JanusGraphClient()), healingJanusGraphDao);

            GraphVertex baseTypeVertex = mock(GraphVertex.class);
            when(baseTypeVertex.getMetadataProperty(GraphPropertyEnum.VERSION)).thenReturn("1.0");
            when(healingJanusGraphDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), any(), isNull(), eq(JsonParseFlagEnum.ParseAll), any()))
                .thenReturn(Either.left(Collections.singletonList(baseTypeVertex)));

            GraphVertex derivedTypeVertex = mock(GraphVertex.class);
            when(derivedTypeVertex.getMetadataProperty(GraphPropertyEnum.STATE)).thenReturn(LifecycleStateEnum.CERTIFIED.name());
            when(derivedTypeVertex.getMetadataProperty(GraphPropertyEnum.VERSION)).thenReturn("1.0");

            GraphVertex derivedTypeVertexUncertified = mock(GraphVertex.class);
            when(derivedTypeVertexUncertified.getMetadataProperty(GraphPropertyEnum.STATE)).thenReturn(
                LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name());
            when(derivedTypeVertexUncertified.getMetadataProperty(GraphPropertyEnum.VERSION)).thenReturn("1.1");

            when(healingJanusGraphDao.getParentVertices(baseTypeVertex, EdgeLabelEnum.DERIVED_FROM,
                JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(Collections.singletonList(derivedTypeVertex)));
            when(healingJanusGraphDao.getParentVertices(derivedTypeVertex, EdgeLabelEnum.DERIVED_FROM,
                JsonParseFlagEnum.ParseAll)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
            when(derivedTypeVertex.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME)).thenReturn("org.parent.type");

            List<BaseType> baseTypes = elementOperation.getServiceBaseTypes("serviceCategoryA", null);

            assertEquals(2, baseTypes.size());
            assertEquals("org.base.type", baseTypes.get(0).getToscaResourceName());
            assertEquals(1, baseTypes.get(0).getVersions().size());
            assertEquals("1.0", baseTypes.get(0).getVersions().get(0));
            assertEquals("org.parent.type", baseTypes.get(1).getToscaResourceName());
        } finally {
            configurationManager.getConfiguration().setServiceBaseNodeTypes(preExistingServiceNodeTypes);
            configurationManager.getConfiguration().setGenericAssetNodeTypes(preExistingGenericNodeTypes);
        }
    }

    @Test
    public void testBaseTypes_default() {
        Map<String, CategoryBaseTypeConfig> preExistingServiceNodeTypes = configurationManager.getConfiguration().getServiceBaseNodeTypes();
        Map<String, String> preExistingGenericNodeTypes =
            configurationManager.getConfiguration().getGenericAssetNodeTypes();

        try {
            Map<String, String> genericNodeTypes = new HashMap<>();
            genericNodeTypes.put("Service", "org.service.default");
            configurationManager.getConfiguration().setGenericAssetNodeTypes(genericNodeTypes);
            configurationManager.getConfiguration().setServiceBaseNodeTypes(null);

            HealingJanusGraphDao healingJanusGraphDao = mock(HealingJanusGraphDao.class);
            final var elementOperation = new ElementOperation(new JanusGraphGenericDao(new JanusGraphClient()), healingJanusGraphDao);

            GraphVertex baseTypeVertex = mock(GraphVertex.class);
            when(baseTypeVertex.getMetadataProperty(GraphPropertyEnum.VERSION)).thenReturn("1.0");
            when(healingJanusGraphDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), any(), isNull(), eq(JsonParseFlagEnum.ParseAll), any()))
                .thenReturn(Either.left(Collections.singletonList(baseTypeVertex)));

            when(healingJanusGraphDao.getParentVertices(baseTypeVertex, EdgeLabelEnum.DERIVED_FROM,
                JsonParseFlagEnum.ParseAll)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));

            List<BaseType> baseTypes = elementOperation.getServiceBaseTypes("serviceCategoryA", null);

            assertEquals(1, baseTypes.size());
            assertEquals("org.service.default", baseTypes.get(0).getToscaResourceName());
            assertEquals(1, baseTypes.get(0).getVersions().size());
        } finally {
            configurationManager.getConfiguration().setServiceBaseNodeTypes(preExistingServiceNodeTypes);
            configurationManager.getConfiguration().setGenericAssetNodeTypes(preExistingGenericNodeTypes);
        }
    }
}
