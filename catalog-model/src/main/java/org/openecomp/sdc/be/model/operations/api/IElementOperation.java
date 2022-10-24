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
package org.openecomp.sdc.be.model.operations.api;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.BaseType;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.resources.data.CategoryData;

public interface IElementOperation {

    Either<List<CategoryDefinition>, ActionStatus> getAllResourceCategories();

    Either<List<CategoryDefinition>, ActionStatus> getAllServiceCategories();

    Either<List<CategoryDefinition>, ActionStatus> getAllProductCategories();

    Either<List<Tag>, ActionStatus> getAllTags();

    Either<List<PropertyScope>, ActionStatus> getAllPropertyScopes();

    List<ArtifactType> getAllArtifactTypes();

    Either<Configuration.HeatDeploymentArtifactTimeout, ActionStatus> getDefaultHeatTimeout();

    <T extends GraphNode> Either<CategoryData, StorageOperationStatus> getCategoryData(String name, NodeTypeEnum type, Class<T> clazz);

    <T extends GraphNode> Either<org.openecomp.sdc.be.resources.data.category.CategoryData, StorageOperationStatus> getNewCategoryData(String name,
                                                                                                                                       NodeTypeEnum type,
                                                                                                                                       Class<T> clazz);

    Either<Map<String, String>, ActionStatus> getResourceTypesMap();

    Either<CategoryDefinition, ActionStatus> createCategory(CategoryDefinition category, NodeTypeEnum nodeType);

    Either<CategoryDefinition, ActionStatus> createCategory(CategoryDefinition category, NodeTypeEnum nodeType, boolean inTransaction);

    Either<CategoryDefinition, ActionStatus> updateCategory(CategoryDefinition category, NodeTypeEnum nodeType);

    Either<CategoryDefinition, ActionStatus> updateCategory(CategoryDefinition category, NodeTypeEnum nodeType, boolean inTransaction);

    Either<CategoryDefinition, ActionStatus> deleteCategory(NodeTypeEnum nodeType, String categoryId);

    Either<SubCategoryDefinition, ActionStatus> deleteSubCategory(NodeTypeEnum nodeType, String subCategoryId);

    Either<Boolean, ActionStatus> isCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName);

    Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType);

    Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType,
                                                                  boolean inTransaction);

    Either<SubCategoryDefinition, ActionStatus> updateSubCategory(String subCategoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType);

    Either<SubCategoryDefinition, ActionStatus> updateSubCategory(String subCategoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType,
                                                                  boolean inTransaction);

    Either<List<CategoryDefinition>, ActionStatus> getAllCategories(NodeTypeEnum nodeType, boolean inTransaction);
    
    List<BaseType> getServiceBaseTypes(String categoryName, String modelName);

    /**
     * Checks if a category requires a base type.
     *
     * @param categoryName the category name
     * @return {@code true} if a base type is required, {@code false} otherwise.
     */
    boolean isBaseTypeRequired(String categoryName);

    String getDefaultBaseType(String categoryName);

    Either<CategoryDefinition, ActionStatus> getCategory(NodeTypeEnum nodeType, String categoryId);

    Either<SubCategoryDefinition, ActionStatus> getSubCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName);

    Either<Boolean, ActionStatus> isSubCategoryUniqueForCategory(NodeTypeEnum nodeType, String subCategoryNormName, String parentCategoryId);

    Either<GroupingDefinition, ActionStatus> createGrouping(String subCategoryId, GroupingDefinition grouping, NodeTypeEnum nodeType);

    Either<GroupingDefinition, ActionStatus> deleteGrouping(NodeTypeEnum nodeType, String groupingId);

    Either<SubCategoryDefinition, ActionStatus> getSubCategory(NodeTypeEnum nodeType, String subCategoryId);

    Either<Boolean, ActionStatus> isGroupingUniqueForSubCategory(NodeTypeEnum nodeType, String groupingNormName, String parentSubCategoryId);

    Either<GroupingDefinition, ActionStatus> getGroupingUniqueForType(NodeTypeEnum nodeType, String groupingNormalizedName);
}
