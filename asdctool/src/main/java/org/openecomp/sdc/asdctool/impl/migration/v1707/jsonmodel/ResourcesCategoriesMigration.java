package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.MigrationMsg;
import org.openecomp.sdc.asdctool.impl.migration.Migration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.CategoriesUtils.filterOldCategories;
import static org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.CategoriesUtils.filterOldSubCategories;


public class ResourcesCategoriesMigration implements Migration {

    @Resource(name = "element-operation")
    private IElementOperation elementOperation;

    @Resource(name = "element-operation-migration")
    private IElementOperation elementOperationMigration;

    @Resource(name = "titan-dao")
    TitanDao titanDao;

    @Override
    public String description() {
        return "migrate resource categories";
    }

    @Override
    public boolean migrate() {
        return getCategoriesToMigrate().either(this::migrateCategories,
                                               errorStatus -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_RETRIEVE_CATEGORIES.getMessage(errorStatus.name())));
    }

    private Either<List<CategoryDefinition>, ActionStatus> getCategoriesToMigrate() {
        return elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false);
    }

    private boolean migrateCategories(List<CategoryDefinition> categoryDefinitions) {
        List<CategoryDefinition> categoriesToMigrate = filterOldCategoriesAndSubCategories(categoryDefinitions);
        for (CategoryDefinition categoryDefinition : categoriesToMigrate) {
            boolean isMigrated = migrateCategoryIfNotExists(categoryDefinition);
            if (!isMigrated) {
                titanDao.rollback();
                return false;
            }
            titanDao.commit();
        }
        return true;
    }


    //since production was malformed we need to fixed it by removing wrong categories and sub categories
    private List<CategoryDefinition> filterOldCategoriesAndSubCategories(List<CategoryDefinition> categoryDefinitions) {
        Map<String, List<CategoryDefinition>> categoriesByNormalName = categoryDefinitions.stream().collect(Collectors.groupingBy(CategoryDataDefinition::getNormalizedName));
        List<CategoryDefinition> categoriesToMigrate = filterOldCategories(categoryDefinitions);
        for (CategoryDefinition categoryDefinition : categoriesToMigrate) {
            List<SubCategoryDefinition> newSubCategories = getAllDistinctSubCategories(categoriesByNormalName.get(categoryDefinition.getNormalizedName()));
            categoryDefinition.setSubcategories(newSubCategories);
        }
        return categoriesToMigrate;
    }

    private List<SubCategoryDefinition> getAllDistinctSubCategories (List<CategoryDefinition> categoriesDefinitions) {
        Map<String, List<SubCategoryDefinition>> subCategoriesByNormalName = categoriesDefinitions.stream().flatMap(ct -> ct.getSubcategories().stream()).collect(Collectors.groupingBy(SubCategoryDefinition::getNormalizedName));
        return getDistinctSubCategories(subCategoriesByNormalName);
    }

    private List<SubCategoryDefinition> getDistinctSubCategories(Map<String, List<SubCategoryDefinition>> subCategoriesByNormalName) {
        List<SubCategoryDefinition> allSubCategories = new ArrayList<>();
        for (List<SubCategoryDefinition> subCategoryDefinitions : subCategoriesByNormalName.values()) {
            if (subCategoryDefinitions.size() == 1) {
                allSubCategories.addAll(subCategoryDefinitions);
            } else {
                allSubCategories.addAll(filterOldSubCategories(subCategoryDefinitions));
            }
        }
        return allSubCategories;
    }

    private boolean migrateCategoryIfNotExists(CategoryDefinition categoryDefinition) {
        return isExists(categoryDefinition).either(isExist -> isExist ? migrateSubCategories(categoryDefinition) : migrateCategoryAndSubCategories(categoryDefinition),
                                                   error -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_RETRIEVE_CATEGORY.getMessage(categoryDefinition.getName(), error.name())));
    }

    private boolean migrateCategoryAndSubCategories(CategoryDefinition resourceCategory) {
        return elementOperationMigration.createCategory(resourceCategory, NodeTypeEnum.ResourceNewCategory)
                .either(createdCategory -> this.migrateSubCategories(resourceCategory),
                        status -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_CREATE_CATEGORY.getMessage(resourceCategory.getName(), status.name())));
    }

    private boolean migrateSubCategories(CategoryDefinition categoryDefinition) {
        for (SubCategoryDefinition subCategory : categoryDefinition.getSubcategories()) {
            boolean isMigrated = migrateSubcategoryIfNotExists(categoryDefinition, subCategory);
            if (!isMigrated) {
                return false;
            }
        }
        return true;
    }

    private boolean migrateSubcategoryIfNotExists(CategoryDefinition parentCategory, SubCategoryDefinition subCategory) {
        return isExists(subCategory).either(isExists -> isExists || migrateSubCategory(parentCategory, subCategory),
                                            status -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_RETRIEVE_CATEGORY.getMessage(subCategory.getName(), status.name())));
    }

    private boolean migrateSubCategory(CategoryDefinition categoryDefinition, SubCategoryDefinition subCategory) {
        return elementOperationMigration.createSubCategory(categoryDefinition.getUniqueId(), subCategory, NodeTypeEnum.ResourceSubcategory)
                .either(createdSubCategory -> true,
                        errorStatus -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_CREATE_SUB_CATEGORY.getMessage(subCategory.getName(), categoryDefinition.getName(), errorStatus.name())));

    }

    private Either<Boolean, ActionStatus> isExists(CategoryDefinition category) {
        Either<CategoryDefinition, ActionStatus> byId = getCategoryById(category.getUniqueId());
        return byId.either(existingVal -> Either.left(true),
                           this::getEitherNotExistOrErrorStatus);
    }

    private Either<Boolean, ActionStatus> isExists(SubCategoryDefinition subCategory) {
        return getSubCategoryById(subCategory.getUniqueId()).either(existingVal -> Either.left(true),
                                               this::getEitherNotExistOrErrorStatus);
    }

    private Either<Boolean, ActionStatus> getEitherNotExistOrErrorStatus(ActionStatus status) {
        return status == ActionStatus.COMPONENT_CATEGORY_NOT_FOUND ? Either.left(false) : Either.right(status);
    }

    private Either<CategoryDefinition, ActionStatus> getCategoryById(String uid) {
        return elementOperationMigration.getCategory(NodeTypeEnum.ResourceNewCategory, uid);
    }

    private Either<SubCategoryDefinition, ActionStatus> getSubCategoryById(String uid) {
        return elementOperationMigration.getSubCategory(NodeTypeEnum.ResourceSubcategory, uid);
    }


}
