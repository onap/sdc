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

package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.MigrationMsg;
import org.openecomp.sdc.asdctool.impl.migration.Migration1707Task;
import org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.CategoriesUtils.filterOldCategories;
import static org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.CategoriesUtils.filterOldSubCategories;


public class ResourcesCategoriesMigration implements Migration1707Task {

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
        Map<String, List<SubCategoryDefinition>> subCategoriesByNormalName = categoriesDefinitions.stream()
                .filter(ct -> ct.getSubcategories()!=null)
                .flatMap(ct -> ct.getSubcategories().stream())
                .collect(Collectors.groupingBy(SubCategoryDefinition::getNormalizedName));
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
        return isExists(parentCategory, subCategory).either(isExists -> isExists || migrateSubCategory(parentCategory, subCategory),
                                            status -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_RETRIEVE_CATEGORY.getMessage(subCategory.getName(), status.name())));
    }

    private boolean migrateSubCategory(CategoryDefinition categoryDefinition, SubCategoryDefinition subCategory) {
        return elementOperationMigration.createSubCategory(categoryDefinition.getUniqueId(), subCategory, NodeTypeEnum.ResourceSubcategory)
                .either(createdSubCategory -> true,
                        errorStatus -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_CREATE_SUB_CATEGORY.getMessage(subCategory.getName(), categoryDefinition.getName(), errorStatus.name())));

    }

    private Either<Boolean, ActionStatus> isExists(CategoryDefinition category) {
        Either<CategoryDefinition, ActionStatus> byId = getCategoryById(category);
        return byId.either(existingVal -> Either.left(true),
                           this::getEitherNotExistOrErrorStatus);
    }

    private Either<Boolean, ActionStatus> isExists(CategoryDefinition parentCategory, SubCategoryDefinition subCategory) {
        return getSubCategoryById(parentCategory, subCategory).either(existingVal -> Either.left(true),
                                               this::getEitherNotExistOrErrorStatus);
    }

    private Either<Boolean, ActionStatus> getEitherNotExistOrErrorStatus(ActionStatus status) {
        return status == ActionStatus.COMPONENT_CATEGORY_NOT_FOUND ? Either.left(false) : Either.right(status);
    }

    private Either<CategoryDefinition, ActionStatus> getCategoryById(CategoryDefinition category) {
        return elementOperationMigration.getCategory(NodeTypeEnum.ResourceNewCategory, category.getUniqueId());
    }

    private Either<SubCategoryDefinition, ActionStatus> getSubCategoryById(CategoryDefinition parentCategory, SubCategoryDefinition subCategory) {
        String subCategoryUid = getExpectedSubCategoryId(parentCategory, subCategory);
        return elementOperationMigration.getSubCategory(NodeTypeEnum.ResourceSubcategory, subCategoryUid);
    }

    //since a sub category might belong to a different category in old graph its new graph id is different than its old graph id
    private String getExpectedSubCategoryId(CategoryDefinition parentCategory, SubCategoryDefinition subCategory) {
        String parentId = UniqueIdBuilder.buildCategoryUid(parentCategory.getNormalizedName(), NodeTypeEnum.ResourceNewCategory);
        return UniqueIdBuilder.buildSubCategoryUid(parentId, subCategory.getNormalizedName());
    }


}
