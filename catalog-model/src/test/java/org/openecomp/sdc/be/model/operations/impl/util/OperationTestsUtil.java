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

package org.openecomp.sdc.be.model.operations.impl.util;

import fj.data.Either;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ResourceCategoryData;
import org.openecomp.sdc.be.resources.data.ServiceCategoryData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.common.util.ValidationUtils;

public class OperationTestsUtil {

    public static String deleteAndCreateServiceCategory(String category, JanusGraphGenericDao janusGraphDao) {
        CategoryData categoryData = new CategoryData(NodeTypeEnum.ServiceNewCategory);
        categoryData.getCategoryDataDefinition().setName(category);
        categoryData.getCategoryDataDefinition()
                .setNormalizedName(ValidationUtils.normalizeCategoryName4Uniqueness(category));
        categoryData.getCategoryDataDefinition().setUniqueId(UniqueIdBuilder.buildCategoryUid(
                ValidationUtils.normalizeCategoryName4Uniqueness(category), NodeTypeEnum.ServiceNewCategory));
        janusGraphDao.deleteNode(categoryData, CategoryData.class);
        Either<CategoryData, JanusGraphOperationStatus> createNode = janusGraphDao.createNode(categoryData, CategoryData.class);
        return (String) createNode.left().value().getUniqueId();
    }

    public static String deleteAndCreateResourceCategory(String category, String subcategory,
            JanusGraphGenericDao janusGraphDao) {

        CategoryData categoryData = new CategoryData(NodeTypeEnum.ResourceNewCategory);
        categoryData.getCategoryDataDefinition().setName(category);
        categoryData.getCategoryDataDefinition()
                .setNormalizedName(ValidationUtils.normalizeCategoryName4Uniqueness(category));
        categoryData.getCategoryDataDefinition().setUniqueId(UniqueIdBuilder.buildCategoryUid(
                ValidationUtils.normalizeCategoryName4Uniqueness(category), NodeTypeEnum.ResourceNewCategory));

        SubCategoryData subcategoryData = new SubCategoryData(NodeTypeEnum.ResourceSubcategory);
        subcategoryData.getSubCategoryDataDefinition().setName(subcategory);
        subcategoryData.getSubCategoryDataDefinition()
                .setNormalizedName(ValidationUtils.normalizeCategoryName4Uniqueness(subcategory));
        subcategoryData.getSubCategoryDataDefinition().setUniqueId(UniqueIdBuilder
                .buildSubCategoryUid(categoryData.getCategoryDataDefinition().getUniqueId(), subcategory));
        janusGraphDao.deleteNode(categoryData, CategoryData.class);
        janusGraphDao.deleteNode(subcategoryData, SubCategoryData.class);
        Either<CategoryData, JanusGraphOperationStatus> createNode = janusGraphDao.createNode(categoryData, CategoryData.class);
        janusGraphDao.createNode(subcategoryData, SubCategoryData.class);
        janusGraphDao.createRelation(categoryData, subcategoryData, GraphEdgeLabels.SUB_CATEGORY, null);
        return (String) createNode.left().value().getUniqueId();
    }

    public static void deleteServiceCategory(String category, JanusGraphGenericDao janusGraphDao) {
        ServiceCategoryData categoryData = new ServiceCategoryData(category);
        janusGraphDao.deleteNode(categoryData, ServiceCategoryData.class);
    }

    public static void deleteResourceCategory(String category, String subcategory, JanusGraphGenericDao janusGraphDao) {
        ResourceCategoryData categoryData = new ResourceCategoryData(category, subcategory);
        janusGraphDao.deleteNode(categoryData, ResourceCategoryData.class);
    }

    public static User convertUserDataToUser(UserData modifierData) {
        User modifier = new User();
        modifier.setUserId(modifierData.getUserId());
        modifier.setEmail(modifierData.getEmail());
        modifier.setFirstName(modifierData.getFirstName());
        modifier.setLastName(modifierData.getLastName());
        modifier.setRole(modifierData.getRole());
        return modifier;
    }
}
