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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ElementOperation;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.be.ui.model.UiCategories;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by chaya on 12/7/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElementBLTest {

    private static final String CATEGORY_NAME = "categoryName";
    private static final String CATEGORY_UNIQUE_ID = "catUniqueId";
    private static final String SUBCATEGORY_UNIQUE_ID = "subCatUniqeId";
    private static final String PROPER_COMPONENT_ID = "properComponentId";
    private static final String DELETED_COMPONENT_ID = "deletedId";
    private static final String ARCHIVED_COMPONENT_ID = "archivedId";
    private static final String NOT_HIGHEST_VERSION_ID = "notHighestVersionId";

    private GraphVertex categoryVertex = new GraphVertex(VertexTypeEnum.RESOURCE_CATEGORY);
    private List<GraphVertex> deletedAndNotDeletedResourceVertices = new ArrayList<>();
    private List<GraphVertex> archivedAndNotArchivedResourceVertices = new ArrayList<>();
    private List<GraphVertex> notHighestVersionAndHighestVersionResourceVertices = new ArrayList<>();
    private List<GraphVertex> deletedAndNotDeletedServiceVertices = new ArrayList<>();

    private Resource properResource = new Resource();

    private Service properService = new Service();

    private List<CategoryData> categories = new ArrayList<>();
    private List<SubCategoryData> subCategories = new ArrayList<>();


    @Mock
    private JanusGraphGenericDao janusGraphGenericDao;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private UserValidations userValidations;

    @Mock
    private ElementOperation elementOperation;

    @Mock
    private UserBusinessLogic userAdminManager;

    @Mock
    protected ComponentsUtils componentsUtils;

    @Mock
    private SubCategoryDefinition subCategoryDef;

    @Mock
    private CategoryDefinition categoryDef;

    @InjectMocks
    private ElementBusinessLogic elementBusinessLogic;


    @Before
    public void setup() {
        initCategoriesAndSubCategories();
        initResourcesVerticesLists();
        initServiceVerticesLists();
        elementBusinessLogic.setUserValidations(userValidations);
        elementBusinessLogic.setComponentsUtils(componentsUtils);
        elementBusinessLogic.setJanusGraphGenericDao(janusGraphGenericDao);
        elementBusinessLogic.setJanusGraphDao(janusGraphDao);
        elementBusinessLogic.setToscaOperationFacade(toscaOperationFacade);

        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

    }

    private void initCategoriesAndSubCategories() {
        CategoryData categoryData = new CategoryData(NodeTypeEnum.ServiceNewCategory);
        categoryData.getCategoryDataDefinition().setUniqueId(CATEGORY_UNIQUE_ID);
        categories.add(categoryData);

        SubCategoryData subCategoryData = new SubCategoryData(NodeTypeEnum.ResourceNewCategory);
        subCategoryData.getSubCategoryDataDefinition().setUniqueId(SUBCATEGORY_UNIQUE_ID);
        subCategories.add(subCategoryData);
    }

    private void initServiceVerticesLists() {
        Map<String, Object> properServiceMetadataJson = new HashMap<>();
        properServiceMetadataJson.put(JsonPresentationFields.IS_DELETED.getPresentation(), false);
        properServiceMetadataJson.put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), true);
        properServiceMetadataJson.put(JsonPresentationFields.IS_ARCHIVED.getPresentation(), false);
        GraphVertex properService = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        properService.setType(ComponentTypeEnum.SERVICE);
        properService.setMetadataJson(properServiceMetadataJson);
        properService.setUniqueId(PROPER_COMPONENT_ID);
        deletedAndNotDeletedServiceVertices.add(properService);

        Map<String, Object> deletedServiceMetadataJson = new HashMap<>();
        deletedServiceMetadataJson.put(JsonPresentationFields.IS_DELETED.getPresentation(), true);
        deletedServiceMetadataJson.put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), true);
        deletedServiceMetadataJson.put(JsonPresentationFields.IS_ARCHIVED.getPresentation(), false);
        GraphVertex deletedService = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        deletedService.setType(ComponentTypeEnum.SERVICE);
        deletedService.setMetadataJson(deletedServiceMetadataJson);
        deletedService.setUniqueId(DELETED_COMPONENT_ID);
        deletedAndNotDeletedServiceVertices.add(deletedService);
    }

    private void initResourcesVerticesLists() {
        Map<String, Object> properResourceMetadataJson = new HashMap<>();
        properResourceMetadataJson.put(JsonPresentationFields.IS_DELETED.getPresentation(), false);
        properResourceMetadataJson.put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), true);
        properResourceMetadataJson.put(JsonPresentationFields.IS_ARCHIVED.getPresentation(), false);
        properResourceMetadataJson.put(JsonPresentationFields.RESOURCE_TYPE.getPresentation(), ResourceTypeEnum.VFC.getValue());
        GraphVertex properResource = new GraphVertex(VertexTypeEnum.NODE_TYPE);
        properResource.setType(ComponentTypeEnum.RESOURCE);
        properResource.setMetadataJson(properResourceMetadataJson);
        properResource.setUniqueId(PROPER_COMPONENT_ID);
        deletedAndNotDeletedResourceVertices.add(properResource);
        archivedAndNotArchivedResourceVertices.add(properResource);
        notHighestVersionAndHighestVersionResourceVertices.add(properResource);

        Map<String, Object> deletedResourceMetadataJson = new HashMap<>();
        deletedResourceMetadataJson.put(JsonPresentationFields.IS_DELETED.getPresentation(), true);
        deletedResourceMetadataJson.put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), true);
        deletedResourceMetadataJson.put(JsonPresentationFields.IS_ARCHIVED.getPresentation(), false);
        deletedResourceMetadataJson.put(JsonPresentationFields.RESOURCE_TYPE.getPresentation(), ResourceTypeEnum.VFC.getValue());
        GraphVertex deletedResource = new GraphVertex(VertexTypeEnum.NODE_TYPE);
        deletedResource.setType(ComponentTypeEnum.RESOURCE);
        deletedResource.setMetadataJson(deletedResourceMetadataJson);
        deletedResource.setUniqueId(DELETED_COMPONENT_ID);
        deletedAndNotDeletedResourceVertices.add(deletedResource);

        Map<String, Object> archivedResourceMetadataJson = new HashMap<>();
        archivedResourceMetadataJson.put(JsonPresentationFields.IS_DELETED.getPresentation(), false);
        archivedResourceMetadataJson.put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), true);
        archivedResourceMetadataJson.put(JsonPresentationFields.IS_ARCHIVED.getPresentation(), true);
        archivedResourceMetadataJson.put(JsonPresentationFields.RESOURCE_TYPE.getPresentation(), ResourceTypeEnum.VFC.getValue());
        GraphVertex archivedResource = new GraphVertex(VertexTypeEnum.NODE_TYPE);
        archivedResource.setType(ComponentTypeEnum.RESOURCE);
        archivedResource.setMetadataJson(archivedResourceMetadataJson);
        archivedResource.setUniqueId(ARCHIVED_COMPONENT_ID);
        archivedAndNotArchivedResourceVertices.add(archivedResource);

        Map<String, Object> notHighestVersionResourceMetadataJson = new HashMap<>();
        notHighestVersionResourceMetadataJson.put(JsonPresentationFields.IS_DELETED.getPresentation(), false);
        notHighestVersionResourceMetadataJson.put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), false);
        notHighestVersionResourceMetadataJson.put(JsonPresentationFields.IS_ARCHIVED.getPresentation(), false);
        notHighestVersionResourceMetadataJson.put(JsonPresentationFields.RESOURCE_TYPE.getPresentation(), ResourceTypeEnum.VFC.getValue());
        GraphVertex notHighestVersionResource = new GraphVertex(VertexTypeEnum.NODE_TYPE);
        notHighestVersionResource.setType(ComponentTypeEnum.RESOURCE);
        notHighestVersionResource.setMetadataJson(notHighestVersionResourceMetadataJson);
        notHighestVersionResource.setUniqueId(NOT_HIGHEST_VERSION_ID);
        notHighestVersionAndHighestVersionResourceVertices.add(notHighestVersionResource);
    }

    @Test
    public void testFetchByCategoryOrSubCategoryUid_deletedResource() {
        when(janusGraphDao.getVertexById(CATEGORY_UNIQUE_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(categoryVertex));
        when(janusGraphDao.getParentVertices(categoryVertex, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(deletedAndNotDeletedResourceVertices));
        when(toscaOperationFacade.getToscaElement(PROPER_COMPONENT_ID, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(properResource));
        Either<List<Component>, StorageOperationStatus> resourcesBySubCategoryUidEither = elementBusinessLogic.fetchByCategoryOrSubCategoryUid(CATEGORY_UNIQUE_ID, NodeTypeEnum.Resource, false, null);
        List<Component> resourcesBySubCategoryUid = resourcesBySubCategoryUidEither.left().value();
        assertThat(resourcesBySubCategoryUid.size()).isEqualTo(1);
        assertThat(resourcesBySubCategoryUid.get(0)).isSameAs(properResource);
    }

    @Test
    public void testFetchByCategoryOrSubCategoryUid_archivedResource() {
        when(janusGraphDao.getVertexById(CATEGORY_UNIQUE_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(categoryVertex));
        when(janusGraphDao.getParentVertices(categoryVertex, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(archivedAndNotArchivedResourceVertices));
        when(toscaOperationFacade.getToscaElement(PROPER_COMPONENT_ID, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(properResource));
        Either<List<Component>, StorageOperationStatus> resourcesBySubCategoryUidEither = elementBusinessLogic.fetchByCategoryOrSubCategoryUid(CATEGORY_UNIQUE_ID, NodeTypeEnum.Resource, false, null);
        List<Component> resourcesBySubCategoryUid = resourcesBySubCategoryUidEither.left().value();
        assertThat(resourcesBySubCategoryUid.size()).isEqualTo(1);
        assertThat(resourcesBySubCategoryUid.get(0)).isSameAs(properResource);
    }

    @Test
    public void testFetchByCategoryOrSubCategoryUid_notHighestResource() {
        when(janusGraphDao.getVertexById(CATEGORY_UNIQUE_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(categoryVertex));
        when(janusGraphDao.getParentVertices(categoryVertex, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(notHighestVersionAndHighestVersionResourceVertices));
        when(toscaOperationFacade.getToscaElement(PROPER_COMPONENT_ID, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(properResource));
        Either<List<Component>, StorageOperationStatus> resourcesBySubCategoryUidEither = elementBusinessLogic.fetchByCategoryOrSubCategoryUid(CATEGORY_UNIQUE_ID, NodeTypeEnum.Resource, false, null);
        List<Component> resourcesBySubCategoryUid = resourcesBySubCategoryUidEither.left().value();
        assertThat(resourcesBySubCategoryUid.size()).isEqualTo(1);
        assertThat(resourcesBySubCategoryUid.get(0)).isSameAs(properResource);
    }


    @Test
    public void testFetchByCategoryOrSubCategoryName_resource() {
        Map<String, Object> props = new HashMap<>();
        props.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normalizeCategoryName4Uniqueness(CATEGORY_NAME));
        when(janusGraphGenericDao.getByCriteria(NodeTypeEnum.ResourceNewCategory, props, SubCategoryData.class))
                .thenReturn(Either.left(subCategories));
        when(janusGraphDao.getVertexById(SUBCATEGORY_UNIQUE_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(categoryVertex));
        when(janusGraphDao.getParentVertices(categoryVertex, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(deletedAndNotDeletedResourceVertices));
        when(toscaOperationFacade.getToscaElement(PROPER_COMPONENT_ID, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(properResource));
        Either<List<Component>, StorageOperationStatus> elementsByCategoryEither =
                elementBusinessLogic.fetchByCategoryOrSubCategoryName(CATEGORY_NAME, NodeTypeEnum.ResourceNewCategory,
                        NodeTypeEnum.Resource, false, null);
        List<Component> elementsByCategory = elementsByCategoryEither.left().value();
        assertThat(elementsByCategory.get(0)).isSameAs(properResource);
        assertThat(elementsByCategory.size()).isEqualTo(1);
    }

    @Test
    public void testDeleteSubCategory() {
        Either<SubCategoryDefinition, ResponseFormat> result;
        User user = new User();
        String userId = "userId";
        user.setUserId(userId);
        when(elementBusinessLogic.validateUserExists(anyString())).thenReturn(user);
        when(elementOperation.deleteSubCategory(NodeTypeEnum.ResourceSubcategory, CATEGORY_UNIQUE_ID)).thenReturn(Either.left(subCategoryDef));
        result = elementBusinessLogic.deleteSubCategory(CATEGORY_UNIQUE_ID, ComponentTypeEnum.RESOURCE_PARAM_NAME, userId);
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testDeleteGrouping() {
        Either<GroupingDefinition, ResponseFormat> result;
        GroupingDefinition groupDef = Mockito.mock(GroupingDefinition.class);
        when(elementOperation.deleteGrouping(null, "groupId")).thenReturn(Either.left(groupDef));
        result = elementBusinessLogic.deleteGrouping("groupId", ComponentTypeEnum.RESOURCE_PARAM_NAME, "userId");
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testCreateCategory() {
        Either<CategoryDefinition, ResponseFormat> result;
        CategoryDefinition categoryDef = new CategoryDefinition();
        String name = "name";
        categoryDef.setName(name);
        User user = new User();
        String userId = "userId";
        user.setUserId(userId);
        user.setRole(Role.ADMIN.name());
        when(userValidations.validateUserExists(eq(userId))).thenReturn(user);
        when(elementOperation.isCategoryUniqueForType(NodeTypeEnum.ResourceNewCategory, name)).thenReturn(Either.left(true));
        when(elementOperation.createCategory(categoryDef, NodeTypeEnum.ResourceNewCategory)).thenReturn(Either.left(categoryDef));
        result = elementBusinessLogic.createCategory(categoryDef, ComponentTypeEnum.RESOURCE_PARAM_NAME, userId);
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testGetAllCategories() {
        Either<UiCategories, ResponseFormat> result;
        String userId = "userId";
        List<CategoryDefinition> categoryDefList = new ArrayList<>();
        when(elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false)).thenReturn(Either.left(categoryDefList));
        when(elementOperation.getAllCategories(NodeTypeEnum.ServiceNewCategory, false)).thenReturn(Either.left(categoryDefList));
        result = elementBusinessLogic.getAllCategories(userId);
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testGetAllCategories_NodeType() {
        Either<List<CategoryDefinition>, ResponseFormat> result;
        String userId = "userId";
        List<CategoryDefinition> categoryDefList = new ArrayList<>();
        when(elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false)).thenReturn(Either.left(categoryDefList));
        result = elementBusinessLogic.getAllCategories("resources", userId);
        assertEquals(0, result.left().value().size());
    }

    @Test
    public void testGetCatalogComp_UuidAndAssetType() {
        Either<List<? extends Component>, ResponseFormat> result;
        String uuid = "userId";
        List<Component> components = new ArrayList<>();
        when(toscaOperationFacade.getComponentListByUuid(anyString(), anyMap())).thenReturn(Either.left(components));
        result = elementBusinessLogic.getCatalogComponentsByUuidAndAssetType("resources", uuid);
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testCreateGrouping() {
        Either<GroupingDefinition, ResponseFormat> result;
        GroupingDefinition groupDef = new GroupingDefinition();
        String name = "name";
        groupDef.setName(name);
        String componentTypeParamName = "products";
        String grandParentCatId = "gpCatId";
        String parentSubCatId = "pSubCatId";
        User user = new User();
        String userId = "userId";
        user.setUserId(userId);
        user.setRole(Role.PRODUCT_STRATEGIST.name());
        when(elementBusinessLogic.validateUserExists(userId)).thenReturn(user);
        when(elementOperation.getCategory(NodeTypeEnum.ProductCategory, grandParentCatId)).thenReturn(Either.left(categoryDef));
        when(elementOperation.getSubCategory(NodeTypeEnum.ProductSubcategory, parentSubCatId)).thenReturn(Either.left(subCategoryDef));
        when(elementOperation.isGroupingUniqueForSubCategory(NodeTypeEnum.ProductGrouping, name, parentSubCatId)).thenReturn(Either.left(true));
        when(elementOperation.getGroupingUniqueForType(NodeTypeEnum.ProductGrouping, name)).thenReturn(Either.left(groupDef));
        when(elementOperation.createGrouping(parentSubCatId, groupDef, NodeTypeEnum.ProductGrouping)).thenReturn(Either.left(groupDef));

        result = elementBusinessLogic.createGrouping(groupDef, componentTypeParamName, grandParentCatId, parentSubCatId, userId);
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testFetchByCategoryOrSubCategoryName_service() {
        Map<String, Object> props = new HashMap<>();
        props.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normalizeCategoryName4Uniqueness(CATEGORY_NAME));
        when(janusGraphGenericDao.getByCriteria(NodeTypeEnum.ServiceNewCategory, props, CategoryData.class))
                .thenReturn(Either.left(categories));
        when(janusGraphDao.getVertexById(CATEGORY_UNIQUE_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(categoryVertex));
        when(janusGraphDao.getParentVertices(categoryVertex, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(deletedAndNotDeletedServiceVertices));
        when(toscaOperationFacade.getToscaElement(PROPER_COMPONENT_ID, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(properService));
        Either<List<Component>, StorageOperationStatus> elementsByCategoryEither =
                elementBusinessLogic.fetchByCategoryOrSubCategoryName(CATEGORY_NAME, NodeTypeEnum.ServiceNewCategory,
                        NodeTypeEnum.Service, false, null);
        List<Component> elementsByCategory = elementsByCategoryEither.left().value();
        assertThat(elementsByCategory.get(0)).isSameAs(properService);
        assertThat(elementsByCategory.size()).isEqualTo(1);
    }


}
