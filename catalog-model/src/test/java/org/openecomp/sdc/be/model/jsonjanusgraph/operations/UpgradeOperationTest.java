package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.GraphTestUtils;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class UpgradeOperationTest extends ModelTestBase {

    private boolean isInitialized;
    @Resource
    private JanusGraphDao janusGraphDao;
    @Resource
    private UpgradeOperation upgradeOperation;
    @Resource
    private TopologyTemplateOperation topologyTemplateOperation;
    @Resource
    private NodeTemplateOperation nodeTemplateOperation;
    @Resource
    private UserAdminOperation userAdminOperation; 
    @javax.annotation.Resource
    private IElementOperation elementOperation;

    private User user;

    private CategoryDefinition resourceCategory;
    private CategoryDefinition serviceCategory;
    

    @BeforeClass
    public static void initTest() {
        ModelTestBase.init();
    }

    @Before
    public void beforeTest() {
        if (!isInitialized) {
            GraphTestUtils.clearGraph(janusGraphDao);
            initGraphForTest();
            isInitialized = true;
        }
    }

    @Test
    public void testGetSimpleDependency() {
        
        TopologyTemplate vf = createVf("vf1");
        
        TopologyTemplate service = createServiceWitnInstance("service1", vf);

        
        Either<List<ComponentDependency>, StorageOperationStatus> result = upgradeOperation.getComponentDependencies(vf.getUniqueId());
        assertThat(result.isLeft()).isTrue();
        List<ComponentDependency> dependencies = result.left().value();
        assertThat(dependencies).hasSize(1);
        
        ComponentDependency dependency = dependencies.get(0);
        assertThat(dependency.getName()).isEqualTo(vf.getName());
        assertThat(dependency.getVersion()).isEqualTo(vf.getVersion());
        assertThat(dependency.getDependencies()).hasSize(1);
        
        ComponentDependency container = dependency.getDependencies().get(0);
        assertThat(container.getName()).isEqualTo(service.getName());
        assertThat(container.getVersion()).isEqualTo(service.getVersion());
        assertThat(container.getDependencies()).isNull();

    }

    /*******************************
     * Preperation Methods
     *******************************/
    private void initGraphForTest() {

        user = new User("Jim", "Tom", "jt123a", "1@mail.com", "DESIGNER", System.currentTimeMillis());
        Either<User, StorageOperationStatus> saveUserData = userAdminOperation.saveUserData(user);
        assertThat(saveUserData.isLeft()).isTrue();
        
        GraphTestUtils.createRootCatalogVertex(janusGraphDao);
        resourceCategory = createResourceCategories();
        serviceCategory = createServiceCategory(); 
        
    }

    private TopologyTemplate createServiceWitnInstance(String name, TopologyTemplate createdVf) {
        
        TopologyTemplate serviceNew = createTopologyTemplate(ComponentTypeEnum.SERVICE, name);
        List<CategoryDefinition> categoriesService = new ArrayList<>();
        categoriesService.add(serviceCategory);
        serviceNew.setCategories(categoriesService);

        Either<TopologyTemplate, StorageOperationStatus> createService = topologyTemplateOperation.createTopologyTemplate(serviceNew);
        assertThat(createService.isLeft()).isTrue();
        
        ComponentInstance vfInstance = new ComponentInstance();
        vfInstance.setUniqueId(createdVf.getUniqueId() + createdVf.getName());
        vfInstance.setComponentUid(createdVf.getUniqueId());
        vfInstance.setName(createdVf.getName());
        Either<ImmutablePair<TopologyTemplate, String>, StorageOperationStatus> addInstance = nodeTemplateOperation.addComponentInstanceToTopologyTemplate(serviceNew, createdVf, "0", vfInstance, false, user);
        assertThat(addInstance.isLeft()).isTrue();
        return serviceNew;
    }

    private CategoryDefinition createServiceCategory() {
        CategoryDefinition categoryService = new CategoryDefinition();
        categoryService.setName("servicecategory");
        categoryService.setNormalizedName("servicecategory");
        categoryService.setUniqueId("servicecategory");
        Either<CategoryDefinition, ActionStatus> createCategory = elementOperation.createCategory(categoryService , NodeTypeEnum.ServiceNewCategory);
        
        assertThat(createCategory.isLeft()).isTrue();
        return categoryService;
    }

    private TopologyTemplate createVf(String name) {
        
        TopologyTemplate resource = createTopologyTemplate(ComponentTypeEnum.RESOURCE, name);

        resource.setResourceType(ResourceTypeEnum.VF);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(resourceCategory);
        resource.setCategories(categories);
        Either<TopologyTemplate, StorageOperationStatus> createVf = topologyTemplateOperation.createTopologyTemplate(resource);
        assertThat( createVf.isLeft()).isTrue();
        return resource;
    }

    private CategoryDefinition createResourceCategories() {
        CategoryDefinition category = new CategoryDefinition();
        category.setName("category1");
        category.setNormalizedName("category1");
        category.setUniqueId("category1");
        Either<CategoryDefinition, ActionStatus> createCategory = elementOperation.createCategory(category , NodeTypeEnum.ResourceNewCategory);
        assertThat(createCategory.isLeft()).isTrue();
        
        SubCategoryDefinition subCategory = new SubCategoryDefinition();
        
        subCategory.setName("subcategory1");
        subCategory.setNormalizedName("subcategory1");
        subCategory.setUniqueId("subcategory1");
        elementOperation.createSubCategory(createCategory.left().value().getUniqueId(), subCategory, NodeTypeEnum.ResourceSubcategory);
        category.addSubCategory(subCategory);
        return category;
    }

    private TopologyTemplate createTopologyTemplate(ComponentTypeEnum type, String name) {
        TopologyTemplate template = new TopologyTemplate();
        template.setUniqueId(IdBuilderUtils.generateUniqueId());
        template.setComponentType(type);
        template.setHighestVersion(true);
        template.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        template.setMetadataValue(JsonPresentationFields.NAME, name);
        template.setMetadataValue(JsonPresentationFields.VERSION, "1.0");
        template.setCreatorUserId(user.getUserId());
        return template;
    }
}
