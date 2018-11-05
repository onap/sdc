package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ElementOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by chaya on 12/7/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElementBLTest {

    private static final String CATAGORY_NAME = "categoryName";
    private static final String CATEGORY_UNIQUE_ID = "catUniqueId";
    private static final String SERVICE_NOT_DELETED_ID = "notDeletedId";
    private static final String DELETED_SERVICE_ID = "deletedId";
    private List<CategoryData> categories = new ArrayList<>();
    private List<ImmutablePair<ServiceMetadataData, GraphEdge>> services = new ArrayList<>();
    private List<ImmutablePair<ResourceMetadataData, GraphEdge>> resources = new ArrayList<>();
    private Service notDeletedService = new Service();
    private Resource notDeletedResource =  new Resource();

    @Mock
    private TitanGenericDao titanGenericDao;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private TitanDao titanDao;

    @Mock
    private UserValidations userValidations;

    @Mock
    private ElementOperation elementOperation;

    @InjectMocks
    private ElementBusinessLogic elementBusinessLogic;

    @Before
    public void setup() {

        initCategoriesList();
        initServicesList();
        initResourceslist();

        when(titanDao.commit()).thenReturn(TitanOperationStatus.OK);
    }

    private void initCategoriesList() {
        CategoryData categoryData = new CategoryData(NodeTypeEnum.ServiceNewCategory);
        categoryData.getCategoryDataDefinition().setUniqueId(CATEGORY_UNIQUE_ID);
        categories.add(categoryData);
    }

    private void initServicesList() {
        ServiceMetadataData serviceNotDeleted = new ServiceMetadataData();
        ComponentMetadataDataDefinition componentMetadataDataDefinition1 = new ServiceMetadataDataDefinition();
        componentMetadataDataDefinition1.setIsDeleted(false);
        componentMetadataDataDefinition1.setHighestVersion(true);
        componentMetadataDataDefinition1.setUniqueId(SERVICE_NOT_DELETED_ID);
        serviceNotDeleted.setMetadataDataDefinition(componentMetadataDataDefinition1);
        services.add(new ImmutablePair<>(serviceNotDeleted, null));

        ServiceMetadataData deletedService = new ServiceMetadataData();
        ComponentMetadataDataDefinition componentMetadataDataDefinition2 = new ServiceMetadataDataDefinition();
        componentMetadataDataDefinition2.setIsDeleted(true);
        componentMetadataDataDefinition2.setHighestVersion(true);
        componentMetadataDataDefinition2.setUniqueId(DELETED_SERVICE_ID);
        deletedService.setMetadataDataDefinition(componentMetadataDataDefinition2);
        services.add(new ImmutablePair<>(deletedService, null));
    }

    private void initResourceslist() {
        ResourceMetadataData notDeletedResource = new ResourceMetadataData();
        ComponentMetadataDataDefinition componentMetadataDataDefinition3 = new ResourceMetadataDataDefinition();
        componentMetadataDataDefinition3.setIsDeleted(false);
        componentMetadataDataDefinition3.setHighestVersion(true);
        componentMetadataDataDefinition3.setUniqueId(SERVICE_NOT_DELETED_ID);
        notDeletedResource.setMetadataDataDefinition(componentMetadataDataDefinition3);
        resources.add(new ImmutablePair<>(notDeletedResource, null));

        ResourceMetadataData deletedResource = new ResourceMetadataData();
        ComponentMetadataDataDefinition componentMetadataDataDefinition4 = new ResourceMetadataDataDefinition();
        componentMetadataDataDefinition4.setIsDeleted(true);
        componentMetadataDataDefinition4.setHighestVersion(true);
        componentMetadataDataDefinition4.setUniqueId(DELETED_SERVICE_ID);
        deletedResource.setMetadataDataDefinition(componentMetadataDataDefinition4);
        resources.add(new ImmutablePair<>(deletedResource, null));
    }

    @Test
    public void testFetchElementsByCategoryName_filterDeleted() {
        ArgumentCaptor<Map> criteriaCapture = ArgumentCaptor.forClass(Map.class);

        when(titanGenericDao.getByCriteria(eq(NodeTypeEnum.ServiceNewCategory), criteriaCapture.capture(), eq(CategoryData.class)))
                .thenReturn(Either.left(categories));
        when(titanGenericDao.getParentNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ServiceNewCategory),
                CATEGORY_UNIQUE_ID, GraphEdgeLabels.CATEGORY, NodeTypeEnum.Service, ServiceMetadataData.class))
                .thenReturn(Either.left(services));
        when(toscaOperationFacade.getToscaElement(SERVICE_NOT_DELETED_ID, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(notDeletedService));

        Either<List<Object>, StorageOperationStatus> elementsByCategoryEither =
                elementBusinessLogic.fetchByCategoryOrSubCategoryName(CATAGORY_NAME, NodeTypeEnum.ServiceNewCategory,
                        NodeTypeEnum.Service, false, ServiceMetadataData.class, null);

        List<Object> elementsByCategory = elementsByCategoryEither.left().value();
        assertThat(elementsByCategory.get(0)).isSameAs(notDeletedService);
        assertThat(elementsByCategory.size()).isEqualTo(1);
        verifyCriteriaProperties(criteriaCapture);
    }

    private void verifyCriteriaProperties(ArgumentCaptor<Map> propsCapture) {
        Map<String, Object> props = propsCapture.getValue();
        assertThat(props.size()).isEqualTo(1);
        assertThat(props.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty())).isEqualTo(ValidationUtils.normalizeCategoryName4Uniqueness(CATAGORY_NAME));
    }

    @Test
    public void testFetchResourcesBySubcategoryUid_filterDeleted() {

        when(titanGenericDao.getParentNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceSubcategory),
                CATEGORY_UNIQUE_ID, GraphEdgeLabels.CATEGORY, NodeTypeEnum.Resource, ResourceMetadataData.class))
                .thenReturn(Either.left(resources));

        when(toscaOperationFacade.getToscaElement(SERVICE_NOT_DELETED_ID, JsonParseFlagEnum.ParseMetadata))
                .thenReturn(Either.left(notDeletedResource));

        Either<List<Object>, StorageOperationStatus> resourcesBySubCategoryUidEither = elementBusinessLogic.fetchByCategoryOrSubCategoryUid(CATEGORY_UNIQUE_ID, NodeTypeEnum.ResourceSubcategory,
                NodeTypeEnum.Resource, false, ResourceMetadataData.class, null);
        List<Object> resourcesBySubCategoryUid = resourcesBySubCategoryUidEither.left().value();
        assertThat(resourcesBySubCategoryUid.size()).isEqualTo(1);
        assertThat(resourcesBySubCategoryUid.get(0)).isSameAs(notDeletedResource);
    }

    @Test
    public void testDeleteCategory() {
        Either<CategoryDefinition, ResponseFormat> result;
        User user = new User();
        String userId = "userId";
        user.setUserId(userId);
        CategoryDefinition categoryDef = new CategoryDefinition();
        when(elementBusinessLogic.validateUserExists(anyString(), anyString(), eq(false))).thenReturn(user);
        when(elementOperation.deleteCategory(NodeTypeEnum.ResourceNewCategory, CATEGORY_UNIQUE_ID)).thenReturn(Either.left(categoryDef));
        result = elementBusinessLogic.deleteCategory(CATEGORY_UNIQUE_ID, ComponentTypeEnum.RESOURCE_PARAM_NAME, userId);
        assertThat(result.isLeft());
    }

    @Test
    public void testDeleteSubCategory() {
        Either<SubCategoryDefinition, ResponseFormat> result;
        User user = new User();
        String userId = "userId";
        user.setUserId(userId);
        SubCategoryDefinition subCategoryDef = new SubCategoryDefinition();
        when(elementBusinessLogic.validateUserExists(anyString(), anyString(), eq(false))).thenReturn(user);
        when(elementOperation.deleteSubCategory(NodeTypeEnum.ResourceSubcategory, CATEGORY_UNIQUE_ID)).thenReturn(Either.left(subCategoryDef));
        result = elementBusinessLogic.deleteSubCategory(CATEGORY_UNIQUE_ID, ComponentTypeEnum.RESOURCE_PARAM_NAME, userId);
        assertThat(result.isLeft());
    }
}
