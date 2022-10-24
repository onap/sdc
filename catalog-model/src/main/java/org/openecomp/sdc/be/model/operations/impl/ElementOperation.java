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

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.be.config.ArtifactConfigManager;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.CategoryBaseTypeConfig;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.category.SubCategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.BaseType;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.TagData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.GroupingData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("element-operation")
public class ElementOperation implements IElementOperation {

    private static final String COULDN_T_FETCH_janusGraph_GRAPH = "Couldn't fetch janusgraph graph";
    private static final String UNKNOWN_CATEGORY_TYPE = "Unknown category type {}";
    private static final Logger log = Logger.getLogger(ElementOperation.class.getName());
    private static final String PROBLEM_WHILE_CREATING_CATEGORY_REASON = "Problem while creating category, reason {}";
    private JanusGraphGenericDao janusGraphGenericDao;
    private HealingJanusGraphDao janusGraphDao;

    public ElementOperation(@Qualifier("janusgraph-generic-dao") JanusGraphGenericDao janusGraphGenericDao,
                            @Qualifier("janusgraph-dao") HealingJanusGraphDao janusGraphDao) {
        super();
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.janusGraphDao = janusGraphDao;
    }

    private static NodeTypeEnum getChildNodeType(NodeTypeEnum parentTypeEnum) {
        NodeTypeEnum res = null;
        switch (parentTypeEnum) {
            case ResourceNewCategory:
                res = NodeTypeEnum.ResourceSubcategory;
                break;
            case ProductCategory:
                res = NodeTypeEnum.ProductSubcategory;
                break;
            case ProductSubcategory:
                res = NodeTypeEnum.ProductGrouping;
                break;
            default:
                break;
        }
        return res;
    }

    /*
     * Old flow
     */
    @Override
    public Either<List<CategoryDefinition>, ActionStatus> getAllServiceCategories() {
        return getAllCategories(NodeTypeEnum.ServiceNewCategory, false);
    }

    @Override
    public Either<List<CategoryDefinition>, ActionStatus> getAllResourceCategories() {
        return getAllCategories(NodeTypeEnum.ResourceNewCategory, false);
    }

    @Override
    public Either<List<CategoryDefinition>, ActionStatus> getAllProductCategories() {
        return getAllCategories(NodeTypeEnum.ProductCategory, false);
    }

    /*
     * New flow
     */
    @Override
    public Either<CategoryDefinition, ActionStatus> createCategory(CategoryDefinition category, NodeTypeEnum nodeType) {
        return createCategory(category, nodeType, false);
    }

    @Override
    public Either<CategoryDefinition, ActionStatus> createCategory(CategoryDefinition category, NodeTypeEnum nodeType, boolean inTransaction) {
        Either<CategoryDefinition, ActionStatus> result = null;
        category.setUniqueId(UniqueIdBuilder.buildCategoryUid(category.getNormalizedName(), nodeType));
        CategoryData categoryData = new CategoryData(nodeType, category);
        try {
            Either<CategoryData, JanusGraphOperationStatus> createNode = janusGraphGenericDao.createNode(categoryData, CategoryData.class);
            if (createNode.isRight()) {
                JanusGraphOperationStatus value = createNode.right().value();
                ActionStatus actionStatus = ActionStatus.GENERAL_ERROR;
                log.debug(PROBLEM_WHILE_CREATING_CATEGORY_REASON, value);
                if (value == JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION) {
                    actionStatus = ActionStatus.COMPONENT_CATEGORY_ALREADY_EXISTS;
                }
                result = Either.right(actionStatus);
                return result;
            }
            CategoryDefinition created = new CategoryDefinition(createNode.left().value().getCategoryDataDefinition());
            result = Either.left(created);
            return result;
        } finally {
            if (!inTransaction) {
                if (result != null && result.isLeft()) {
                    janusGraphGenericDao.commit();
                } else {
                    janusGraphGenericDao.rollback();
                }
            }
        }
    }

    @Override
    public Either<CategoryDefinition, ActionStatus> updateCategory(CategoryDefinition category, NodeTypeEnum nodeType) {
        return updateCategory(category, nodeType, false);
    }

    @Override
    public Either<CategoryDefinition, ActionStatus> updateCategory(CategoryDefinition category, NodeTypeEnum nodeType, boolean inTransaction) {
        Either<CategoryDefinition, ActionStatus> result = null;
        category.setUniqueId(UniqueIdBuilder.buildCategoryUid(category.getNormalizedName(), nodeType));
        CategoryData categoryData = new CategoryData(nodeType, category);
        try {
            Either<CategoryData, JanusGraphOperationStatus> updatedNode = janusGraphGenericDao.updateNode(categoryData, CategoryData.class);
            if (updatedNode.isRight()) {
                JanusGraphOperationStatus value = updatedNode.right().value();
                ActionStatus actionStatus = ActionStatus.GENERAL_ERROR;
                log.debug(PROBLEM_WHILE_CREATING_CATEGORY_REASON, value);
                result = Either.right(actionStatus);
                return result;
            }
            CategoryDefinition created = new CategoryDefinition(updatedNode.left().value().getCategoryDataDefinition());
            result = Either.left(created);
            return result;
        } finally {
            if (!inTransaction) {
                if (result != null && result.isLeft()) {
                    janusGraphGenericDao.commit();
                } else {
                    janusGraphGenericDao.rollback();
                }
            }
        }
    }

    @Override
    public Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory,
                                                                         NodeTypeEnum nodeType) {
        return createSubCategory(categoryId, subCategory, nodeType, false);
    }

    @Override
    public Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType,
                                                                         boolean inTransaction) {
        Either<SubCategoryDefinition, ActionStatus> result = null;
        try {
            // create edge from category to sub-category
            Either<CategoryData, JanusGraphOperationStatus> categoryNode = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId, CategoryData.class);
            ActionStatus actionStatus = ActionStatus.GENERAL_ERROR;
            if (categoryNode.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = categoryNode.right().value();
                log.debug("Problem while fetching category, reason {}", janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    actionStatus = ActionStatus.COMPONENT_CATEGORY_NOT_FOUND;
                }
                result = Either.right(actionStatus);
                return result;
            }
            CategoryDataDefinition categoryDataDefinition = categoryNode.left().value().getCategoryDataDefinition();
            subCategory.setUniqueId(UniqueIdBuilder.buildSubCategoryUid(categoryDataDefinition.getUniqueId(), subCategory.getNormalizedName()));
            SubCategoryData subCategoryData = new SubCategoryData(nodeType, subCategory);
            Either<SubCategoryData, JanusGraphOperationStatus> subCategoryNode = janusGraphGenericDao
                .createNode(subCategoryData, SubCategoryData.class);
            if (subCategoryNode.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = subCategoryNode.right().value();
                log.debug(PROBLEM_WHILE_CREATING_CATEGORY_REASON, janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION) {
                    actionStatus = ActionStatus.COMPONENT_SUB_CATEGORY_EXISTS_FOR_CATEGORY;
                }
                result = Either.right(actionStatus);
                return result;
            }
            Either<GraphRelation, JanusGraphOperationStatus> relation = janusGraphGenericDao
                .createRelation(categoryNode.left().value(), subCategoryNode.left().value(), GraphEdgeLabels.SUB_CATEGORY, null);
            if (relation.isRight()) {
                log.debug("Problem while create relation between category and sub-category ", relation.right().value());
                result = Either.right(actionStatus);
                return result;
            }
            SubCategoryDefinition subCategoryCreated = new SubCategoryDefinition(subCategoryNode.left().value().getSubCategoryDataDefinition());
            result = Either.left(subCategoryCreated);
            return result;
        } finally {
            if (!inTransaction) {
                if (result != null && result.isLeft()) {
                    janusGraphGenericDao.commit();
                } else {
                    janusGraphGenericDao.rollback();
                }
            }
        }
    }

    @Override
    public Either<SubCategoryDefinition, ActionStatus> updateSubCategory(String subCategoryId, SubCategoryDefinition subCategory,
                                                                         NodeTypeEnum nodeType) {
        return updateSubCategory(subCategoryId, subCategory, nodeType, false);
    }

    @Override
    public Either<SubCategoryDefinition, ActionStatus> updateSubCategory(String subCategoryId, SubCategoryDefinition subCategory,
                                                                         NodeTypeEnum nodeType, boolean inTransaction) {
        Either<SubCategoryDefinition, ActionStatus> result = null;
        try {
            subCategory.setUniqueId(subCategoryId);
            SubCategoryData subCategoryData = new SubCategoryData(nodeType, subCategory);
            Either<SubCategoryData, JanusGraphOperationStatus> subCategoryNode = janusGraphGenericDao
                .updateNode(subCategoryData, SubCategoryData.class);
            if (subCategoryNode.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = subCategoryNode.right().value();
                log.debug("Problem while updating sub category, reason {}", janusGraphOperationStatus);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            SubCategoryDefinition subCategoryUpdated = new SubCategoryDefinition(subCategoryNode.left().value().getSubCategoryDataDefinition());
            result = Either.left(subCategoryUpdated);
            return result;
        } finally {
            if (!inTransaction) {
                if (result != null && result.isLeft()) {
                    janusGraphGenericDao.commit();
                } else {
                    janusGraphGenericDao.rollback();
                }
            }
        }
    }

    @Override
    public Either<GroupingDefinition, ActionStatus> createGrouping(String subCategoryId, GroupingDefinition grouping, NodeTypeEnum nodeType) {
        Either<GroupingDefinition, ActionStatus> result = null;
        try {
            // create edge from sub-category to grouping
            Either<SubCategoryData, JanusGraphOperationStatus> subCategoryNode = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
            ActionStatus actionStatus = ActionStatus.GENERAL_ERROR;
            if (subCategoryNode.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = subCategoryNode.right().value();
                log.debug("Problem while fetching category, reason {}", janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION) {
                    actionStatus = ActionStatus.COMPONENT_CATEGORY_NOT_FOUND;
                }
                result = Either.right(actionStatus);
                return result;
            }
            SubCategoryDataDefinition subCatData = subCategoryNode.left().value().getSubCategoryDataDefinition();
            grouping.setUniqueId(UniqueIdBuilder.buildGroupingUid(subCatData.getUniqueId(), grouping.getNormalizedName()));
            GroupingData groupingData = new GroupingData(nodeType, grouping);
            Either<GroupingData, JanusGraphOperationStatus> groupingNode = janusGraphGenericDao.createNode(groupingData, GroupingData.class);
            if (groupingNode.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = groupingNode.right().value();
                log.debug("Problem while creating grouping, reason {}", janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    actionStatus = ActionStatus.COMPONENT_GROUPING_EXISTS_FOR_SUB_CATEGORY;
                }
                result = Either.right(actionStatus);
                return result;
            }
            Either<GraphRelation, JanusGraphOperationStatus> relation = janusGraphGenericDao
                .createRelation(subCategoryNode.left().value(), groupingNode.left().value(), GraphEdgeLabels.GROUPING, null);
            if (relation.isRight()) {
                log.debug("Problem while create relation between sub-category and grouping", relation.right().value());
                result = Either.right(actionStatus);
                return result;
            }
            GroupingDefinition groupingCreated = new GroupingDefinition(groupingNode.left().value().getGroupingDataDefinition());
            result = Either.left(groupingCreated);
            return result;
        } finally {
            if (result != null && result.isLeft()) {
                janusGraphGenericDao.commit();
            } else {
                janusGraphGenericDao.rollback();
            }
        }
    }

    @Override
    public Either<List<CategoryDefinition>, ActionStatus> getAllCategories(NodeTypeEnum nodeType, boolean inTransaction) {
        try {
            if (nodeType != NodeTypeEnum.ResourceNewCategory && nodeType != NodeTypeEnum.ServiceNewCategory
                && nodeType != NodeTypeEnum.ProductCategory) {
                log.debug(UNKNOWN_CATEGORY_TYPE, nodeType.name());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            Either<List<org.openecomp.sdc.be.resources.data.category.CategoryData>, JanusGraphOperationStatus> either = janusGraphGenericDao
                .getAll(nodeType, org.openecomp.sdc.be.resources.data.category.CategoryData.class);
            if (either.isRight() && (either.right().value() != JanusGraphOperationStatus.NOT_FOUND)) {
                log.debug("Problem while get all categories. reason - {}", either.right().value());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            List<CategoryData> categoryDataList = either.isLeft() ? either.left().value() : null;
            List<CategoryDefinition> categoryList = new ArrayList<>();
            if (categoryDataList != null) {
                for (CategoryData elem : categoryDataList) {
                    CategoryDataDefinition categoryDataDefinition = elem.getCategoryDataDefinition();
                    CategoryDefinition categoryDefinition = new CategoryDefinition(categoryDataDefinition);
                    String categoryName = categoryDataDefinition.getName();
                    log.trace("Found category {}, category type {}", categoryName, nodeType);
                    JanusGraphOperationStatus setSubCategories = setSubCategories(nodeType, categoryDefinition);
                    if (setSubCategories != JanusGraphOperationStatus.OK) {
                        log.debug("Failed to set sub-categories for category {}, category type {}, error {}", categoryName, nodeType,
                            setSubCategories);
                        return Either.right(ActionStatus.GENERAL_ERROR);
                    }
                    categoryList.add(categoryDefinition);
                }
            }
            return Either.left(categoryList);
        } finally {
            if (!inTransaction) {
                janusGraphGenericDao.commit();
            }
        }
    }

    @Override
    public List<BaseType> getServiceBaseTypes(final String categoryName, final String modelName) {

        final CategoryBaseTypeConfig categoryBaseTypeConfig = getCategoryBaseTypeConfig(categoryName).orElse(null);
        final List<String> baseToscaResourceNames;
        if (categoryBaseTypeConfig == null) {
            final String generalBaseType = getConfiguration().getGenericAssetNodeTypes().get("Service");
            baseToscaResourceNames = List.of(generalBaseType);
        } else {
            baseToscaResourceNames = getCategoryBaseTypes(categoryName);
        }

        final ArrayList<BaseType> baseTypes = new ArrayList<>();
        baseToscaResourceNames.forEach(baseToscaResourceName -> {
            final Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
            props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, baseToscaResourceName);
            props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
            final Either<List<GraphVertex>, JanusGraphOperationStatus> baseTypeVertex = janusGraphDao
                .getByCriteria(VertexTypeEnum.NODE_TYPE, props, null, JsonParseFlagEnum.ParseAll, modelName);

            if (baseTypeVertex.isLeft()) {
                BaseType baseType = new BaseType(baseToscaResourceName);
                baseTypes.add(baseType);

                final Map<String, List<String>> typesDerivedFromBaseType = new LinkedHashMap<>();
                baseTypeVertex.left().value().forEach(v -> {
                    baseType.addVersion((String) v.getMetadataProperty(GraphPropertyEnum.VERSION));
                    addTypesDerivedFromVertex(typesDerivedFromBaseType, v);
                });

                typesDerivedFromBaseType.forEach((k, v) -> baseTypes.add(new BaseType(k, v)));
            }
        });

        return baseTypes;
    }

    private Configuration getConfiguration() {
        return ConfigurationManager.getConfigurationManager().getConfiguration();
    }
    @Override
    public String getDefaultBaseType(final String categoryName) {
        final Map<String, CategoryBaseTypeConfig> categoriesSpecificBaseTypeMap = getConfiguration().getServiceBaseNodeTypes();
        if (MapUtils.isEmpty(categoriesSpecificBaseTypeMap)) {
            return null;
        }
        final CategoryBaseTypeConfig categoryBaseTypeConfig = categoriesSpecificBaseTypeMap.get(categoryName);
        if (categoryBaseTypeConfig == null) {
            return null;
        }
        return categoryBaseTypeConfig.getDefaultBaseType();
    }

    @Override
    public boolean isBaseTypeRequired(final String categoryName) {
        final Map<String, CategoryBaseTypeConfig> categoriesSpecificBaseTypeMap = getConfiguration().getServiceBaseNodeTypes();
        if (MapUtils.isEmpty(categoriesSpecificBaseTypeMap)) {
            return true;
        }

        final CategoryBaseTypeConfig categoryBaseTypeConfig = categoriesSpecificBaseTypeMap.get(categoryName);
        if (categoryBaseTypeConfig == null) {
            return true;
        }

        return categoryBaseTypeConfig.isRequired();
    }

    private List<String> getCategoryBaseTypes(final String categoryName) {
        final Optional<CategoryBaseTypeConfig> categoryBaseTypeConfigOptional = getCategoryBaseTypeConfig(categoryName);
        if (categoryBaseTypeConfigOptional.isEmpty()) {
            return Collections.emptyList();
        }

        final CategoryBaseTypeConfig categoryBaseTypeConfig = categoryBaseTypeConfigOptional.get();
        if (CollectionUtils.isEmpty(categoryBaseTypeConfig.getBaseTypes())) {
            return Collections.emptyList();
        }

        return categoryBaseTypeConfig.getBaseTypes();
    }

    private Optional<CategoryBaseTypeConfig> getCategoryBaseTypeConfig(final String categoryName) {
        final Map<String, CategoryBaseTypeConfig> categoriesSpecificBaseTypes = getConfiguration().getServiceBaseNodeTypes();
        if (categoriesSpecificBaseTypes == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(categoriesSpecificBaseTypes.get(categoryName));
    }

    private Map<String, List<String>> addTypesDerivedFromVertex(final Map<String, List<String>> types, final GraphVertex vertex) {
        final Either<List<GraphVertex>, JanusGraphOperationStatus> derivedFromVertex =
            janusGraphDao.getParentVertices(vertex, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.ParseAll);
        if (derivedFromVertex.isLeft()) {
            derivedFromVertex.left().value().stream()
                .filter(v -> v.getMetadataProperty(GraphPropertyEnum.STATE).equals(LifecycleStateEnum.CERTIFIED.name()))
                .forEach(v -> {
                    addBaseTypeVersion(types, (String) v.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME),
                        (String) v.getMetadataProperty(GraphPropertyEnum.VERSION));
                    addTypesDerivedFromVertex(types, v);
                });
        }
        return types;
    }

    private void addBaseTypeVersion(final Map<String, List<String>> baseTypes, final String baseTypeToscaResourceName, final String baseTypeVersion) {
        List<String> versions = baseTypes.get(baseTypeToscaResourceName) == null ? new ArrayList<>() : baseTypes.get(baseTypeToscaResourceName);
        versions.add(baseTypeVersion);
        baseTypes.put(baseTypeToscaResourceName, versions);
    }

    private JanusGraphOperationStatus setSubCategories(NodeTypeEnum parentNodeType, CategoryDefinition parentCategory) {
        NodeTypeEnum childNodeType = getChildNodeType(parentNodeType);
        if (childNodeType != null) {
            String categoryName = parentCategory.getName();
            log.trace("Getting sub-categories for category {}, category type {}", categoryName, parentNodeType);
            Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
                .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(parentNodeType), parentCategory.getUniqueId(), GraphEdgeLabels.SUB_CATEGORY,
                    childNodeType, SubCategoryData.class);
            if (parentNode.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    log.trace("Didn't find subcategories for category {}, category type {}", categoryName, parentNodeType);
                    janusGraphOperationStatus = JanusGraphOperationStatus.OK;
                }
                return janusGraphOperationStatus;
            }
            List<ImmutablePair<SubCategoryData, GraphEdge>> subsCategoriesData = parentNode.left().value();
            List<SubCategoryDefinition> subCategoriesDefinitions = new ArrayList<>();
            for (ImmutablePair<SubCategoryData, GraphEdge> subCatPair : subsCategoriesData) {
                SubCategoryDataDefinition subCategoryDataDefinition = subCatPair.getLeft().getSubCategoryDataDefinition();
                SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition(subCategoryDataDefinition);
                log.trace("Found sub-category {} for category {}, category type {}", subCategoryDataDefinition.getName(), categoryName,
                    parentNodeType);
                JanusGraphOperationStatus setGroupings = setGroupings(childNodeType, subCategoryDefinition);
                if (setGroupings != JanusGraphOperationStatus.OK) {
                    log.debug("Failed to set groupings for sub-category {}, sub-category type {}, error {}", subCategoryDataDefinition.getName(),
                        childNodeType, setGroupings);
                    return JanusGraphOperationStatus.GENERAL_ERROR;
                }
                subCategoriesDefinitions.add(subCategoryDefinition);
            }
            parentCategory.setSubcategories(subCategoriesDefinitions);
        }
        return JanusGraphOperationStatus.OK;
    }

    private JanusGraphOperationStatus setGroupings(NodeTypeEnum parentNodeType, SubCategoryDefinition parentSubCategory) {
        NodeTypeEnum childNodeType = getChildNodeType(parentNodeType);
        if (childNodeType != null) {
            String subCategoryName = parentSubCategory.getName();
            log.trace("Getting groupings for subcategory {}, subcategory type {}", subCategoryName, parentNodeType);
            Either<List<ImmutablePair<GroupingData, GraphEdge>>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
                .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(parentNodeType), parentSubCategory.getUniqueId(), GraphEdgeLabels.GROUPING,
                    childNodeType, GroupingData.class);
            if (parentNode.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    log.trace("Didn't find groupings for subcategory {}, subcategory type {}", subCategoryName, parentNodeType);
                    janusGraphOperationStatus = JanusGraphOperationStatus.OK;
                }
                return janusGraphOperationStatus;
            }
            List<ImmutablePair<GroupingData, GraphEdge>> groupingData = parentNode.left().value();
            List<GroupingDefinition> groupingDefinitions = new ArrayList<>();
            for (ImmutablePair<GroupingData, GraphEdge> groupPair : groupingData) {
                GroupingDataDefinition groupingDataDefinition = groupPair.getLeft().getGroupingDataDefinition();
                log.trace("Found grouping {} for sub-category {}, sub-category type {}", groupingDataDefinition.getName(), subCategoryName,
                    parentNodeType);
                groupingDefinitions.add(new GroupingDefinition(groupingDataDefinition));
            }
            parentSubCategory.setGroupings(groupingDefinitions);
        }
        return JanusGraphOperationStatus.OK;
    }

    @Override
    public Either<CategoryDefinition, ActionStatus> getCategory(NodeTypeEnum nodeType, String categoryId) {
        try {
            if (nodeType != NodeTypeEnum.ResourceNewCategory && nodeType != NodeTypeEnum.ServiceNewCategory
                && nodeType != NodeTypeEnum.ProductCategory) {
                log.debug(UNKNOWN_CATEGORY_TYPE, nodeType.name());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            Either<CategoryData, JanusGraphOperationStatus> categoryDataEither = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId, CategoryData.class);
            if (categoryDataEither.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = categoryDataEither.right().value();
                log.debug("Problem while get category by id {}. reason {}", categoryId, janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    return Either.right(ActionStatus.COMPONENT_CATEGORY_NOT_FOUND);
                }
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            CategoryDataDefinition categoryDataDefinition = categoryDataEither.left().value().getCategoryDataDefinition();
            return Either.left(new CategoryDefinition(categoryDataDefinition));
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    @Override
    public Either<SubCategoryDefinition, ActionStatus> getSubCategory(NodeTypeEnum nodeType, String subCategoryId) {
        try {
            if (nodeType != NodeTypeEnum.ResourceSubcategory && nodeType != NodeTypeEnum.ProductSubcategory) {
                log.debug("Unknown sub-category type {}", nodeType.name());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            Either<SubCategoryData, JanusGraphOperationStatus> subCategoryDataEither = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
            if (subCategoryDataEither.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = subCategoryDataEither.right().value();
                log.debug("Problem while get sub-category by id {}. reason {}", subCategoryId, janusGraphOperationStatus);
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    return Either.right(ActionStatus.COMPONENT_CATEGORY_NOT_FOUND);
                }
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            SubCategoryDataDefinition subCategoryDataDefinition = subCategoryDataEither.left().value().getSubCategoryDataDefinition();
            return Either.left(new SubCategoryDefinition(subCategoryDataDefinition));
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    @Override
    public Either<CategoryDefinition, ActionStatus> deleteCategory(NodeTypeEnum nodeType, String categoryId) {
        Either<CategoryDefinition, ActionStatus> result = null;
        try {
            if (nodeType != NodeTypeEnum.ResourceNewCategory && nodeType != NodeTypeEnum.ServiceNewCategory
                && nodeType != NodeTypeEnum.ProductCategory) {
                log.debug(UNKNOWN_CATEGORY_TYPE, nodeType.name());
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Either<CategoryData, JanusGraphOperationStatus> categoryDataEither = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId, CategoryData.class);
            if (categoryDataEither.isRight()) {
                log.debug("Failed to retrieve  category for id {} ", categoryId);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphGenericDao.getGraph();
            if (graph.isRight()) {
                log.debug(COULDN_T_FETCH_janusGraph_GRAPH);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            JanusGraph tGraph = graph.left().value();
            Iterable<JanusGraphVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId).vertices();
            Iterator<JanusGraphVertex> iterator = verticesArtifact.iterator();
            if (!iterator.hasNext()) {
                log.debug("No category node for id = {}", categoryId);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Vertex artifactV = iterator.next();
            artifactV.remove();
            CategoryDefinition deleted = new CategoryDefinition(categoryDataEither.left().value().getCategoryDataDefinition());
            result = Either.left(deleted);
            return result;
        } finally {
            if (result != null && result.isLeft()) {
                janusGraphGenericDao.commit();
            } else {
                janusGraphGenericDao.rollback();
            }
        }
    }

    @Override
    public Either<SubCategoryDefinition, ActionStatus> deleteSubCategory(NodeTypeEnum nodeType, String subCategoryId) {
        Either<SubCategoryDefinition, ActionStatus> result = null;
        try {
            if (nodeType != NodeTypeEnum.ResourceSubcategory && nodeType != NodeTypeEnum.ProductSubcategory) {
                log.debug("Unknown sub-category type {}", nodeType.name());
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Either<SubCategoryData, JanusGraphOperationStatus> subCategoryDataEither = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
            if (subCategoryDataEither.isRight()) {
                log.debug("Failed to retrieve  sub-category for id {}", subCategoryId);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphGenericDao.getGraph();
            if (graph.isRight()) {
                log.debug(COULDN_T_FETCH_janusGraph_GRAPH);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            JanusGraph tGraph = graph.left().value();
            Iterable<JanusGraphVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId).vertices();
            Iterator<JanusGraphVertex> iterator = verticesArtifact.iterator();
            if (!iterator.hasNext()) {
                log.debug("No sub-category node for id {}", subCategoryId);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Vertex artifactV = iterator.next();
            artifactV.remove();

            SubCategoryDefinition deleted = new SubCategoryDefinition(subCategoryDataEither.left().value().getSubCategoryDataDefinition());
            result = Either.left(deleted);
            return result;
        } finally {
            if (result != null && result.isLeft()) {
                janusGraphGenericDao.commit();
            } else {
                janusGraphGenericDao.rollback();
            }
        }
    }

    @Override
    public Either<GroupingDefinition, ActionStatus> deleteGrouping(NodeTypeEnum nodeType, String groupingId) {
        Either<GroupingDefinition, ActionStatus> result = null;
        try {
            if (nodeType != NodeTypeEnum.ProductGrouping) {
                log.debug("Unknown grouping type {}", nodeType.name());
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Either<GroupingData, JanusGraphOperationStatus> groupingDataEither = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), groupingId, GroupingData.class);
            if (groupingDataEither.isRight()) {
                log.debug("Failed to retrieve  grouping for id {}", groupingId);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphGenericDao.getGraph();
            if (graph.isRight()) {
                log.debug(COULDN_T_FETCH_janusGraph_GRAPH);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            JanusGraph tGraph = graph.left().value();
            Iterable<JanusGraphVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), groupingId).vertices();
            Iterator<JanusGraphVertex> iterator = verticesArtifact.iterator();
            if (!iterator.hasNext()) {
                log.debug("No grouping node for id {}", groupingId);
                result = Either.right(ActionStatus.GENERAL_ERROR);
                return result;
            }
            Vertex artifactV = iterator.next();
            artifactV.remove();

            GroupingDefinition deleted = new GroupingDefinition(groupingDataEither.left().value().getGroupingDataDefinition());
            result = Either.left(deleted);
            return result;
        } finally {
            if (result != null && result.isLeft()) {
                janusGraphGenericDao.commit();
            } else {
                janusGraphGenericDao.rollback();
            }
        }
    }

    @Override
    public Either<Boolean, ActionStatus> isCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), normalizedName);
        try {
            Either<List<CategoryData>, JanusGraphOperationStatus> categoryEither = janusGraphGenericDao
                .getByCriteria(nodeType, properties, CategoryData.class);
            if (categoryEither.isRight() && categoryEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("Failed to get categories, nodeType {}, normalizedName {}, error {}", nodeType, normalizedName,
                    categoryEither.right().value());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            List<CategoryData> categoryList = (categoryEither.isLeft() ? categoryEither.left().value() : null);
            if (categoryList != null && categoryList.size() > 0) {
                log.debug("Found category for nodeType {} with normalizedName {}", nodeType, normalizedName);
                if (categoryList.size() > 1) {
                    log.debug("Found more than 1 unique categories for nodeType {} with normalizedName", nodeType, normalizedName);
                    return Either.right(ActionStatus.GENERAL_ERROR);
                }
                return Either.left(false);
            } else {
                log.debug("Category for nodeType {} with normalizedName {} doesn't exist in graph", nodeType, normalizedName);
                return Either.left(true);
            }
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    @Override
    public Either<Boolean, ActionStatus> isSubCategoryUniqueForCategory(NodeTypeEnum nodeType, String subCategoryNormName, String parentCategoryId) {
        String subCategoryId = UniqueIdBuilder.buildSubCategoryUid(parentCategoryId, subCategoryNormName);
        try {
            Either<SubCategoryData, JanusGraphOperationStatus> subCategoryDataEither = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
            if (subCategoryDataEither.isRight() && subCategoryDataEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("Failed to get sub-category with id {}, error {}", subCategoryId, subCategoryDataEither.right().value());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            SubCategoryData subCategoryData = (subCategoryDataEither.isLeft() ? subCategoryDataEither.left().value() : null);
            if (subCategoryData != null) {
                log.debug("Found sub-category with id {}", subCategoryId);
                return Either.left(false);
            } else {
                log.debug("Sub-category for id {} doesn't exist in graph", subCategoryId);
                return Either.left(true);
            }
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    @Override
    public Either<Boolean, ActionStatus> isGroupingUniqueForSubCategory(NodeTypeEnum nodeType, String groupingNormName, String parentSubCategoryId) {
        String groupingId = UniqueIdBuilder.buildGroupingUid(parentSubCategoryId, groupingNormName);
        try {
            Either<GroupingData, JanusGraphOperationStatus> groupingDataEither = janusGraphGenericDao
                .getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), groupingId, GroupingData.class);
            if (groupingDataEither.isRight() && groupingDataEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("Failed to get grouping with id {}, error {}", groupingId, groupingDataEither.right().value());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            GroupingData groupingData = (groupingDataEither.isLeft() ? groupingDataEither.left().value() : null);
            if (groupingData != null) {
                log.debug("Found grouping with id {}", groupingId);
                return Either.left(false);
            } else {
                log.debug("Grouping for id {} doesn't exist in graph", groupingId);
                return Either.left(true);
            }
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    @Override
    public Either<SubCategoryDefinition, ActionStatus> getSubCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), normalizedName);
        try {
            Either<List<SubCategoryData>, JanusGraphOperationStatus> subCategoryEither = janusGraphGenericDao
                .getByCriteria(nodeType, properties, SubCategoryData.class);
            if (subCategoryEither.isRight() && subCategoryEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("Failed to get sub-categories, nodeType {}, normalizedName {}, error {}", nodeType, normalizedName,
                    subCategoryEither.right().value());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            List<SubCategoryData> subCategoryList = (subCategoryEither.isLeft() ? subCategoryEither.left().value() : null);
            if (subCategoryList != null && subCategoryList.size() > 0) {
                log.debug("Found sub-category for nodeType {} with normalizedName {}", nodeType, normalizedName);
                SubCategoryData subCategoryData = subCategoryList.get(0);
                SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition(subCategoryData.getSubCategoryDataDefinition());
                return Either.left(subCategoryDefinition);
            } else {
                log.debug("Sub-category for nodeType {} with normalizedName {} doesn't exist in graph", nodeType, normalizedName);
                return Either.left(null);
            }
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    @Override
    public Either<GroupingDefinition, ActionStatus> getGroupingUniqueForType(NodeTypeEnum nodeType, String groupingNormalizedName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), groupingNormalizedName);
        try {
            Either<List<GroupingData>, JanusGraphOperationStatus> groupingEither = janusGraphGenericDao
                .getByCriteria(nodeType, properties, GroupingData.class);
            if (groupingEither.isRight() && groupingEither.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                log.debug("Failed to get grouping, nodeType {}, normalizedName {}, error {}", nodeType, groupingNormalizedName,
                    groupingEither.right().value());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            List<GroupingData> groupingList = (groupingEither.isLeft() ? groupingEither.left().value() : null);
            if (groupingList != null && groupingList.size() > 0) {
                log.debug("Found grouping for nodeType {} with normalizedName {}", nodeType, groupingNormalizedName);
                GroupingData groupingData = groupingList.get(0);
                GroupingDefinition groupingDefinition = new GroupingDefinition(groupingData.getGroupingDataDefinition());
                return Either.left(groupingDefinition);
            } else {
                log.debug("Grouping for nodeType {} with normalizedName {} doesn't exist in graph", nodeType, groupingNormalizedName);
                return Either.left(null);
            }
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    /*
     *
     */
    @Override
    public Either<List<Tag>, ActionStatus> getAllTags() {
        try {
            Either<List<TagData>, JanusGraphOperationStatus> either = janusGraphGenericDao.getAll(NodeTypeEnum.Tag, TagData.class);
            if (either.isRight()) {
                log.debug("Problem while get all tags. reason - {}", either.right().value());
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
            List<TagData> tagDataList = either.left().value();
            List<Tag> tagList = convertToListOfTag(tagDataList);
            return Either.left(tagList);
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    @Override
    public <T extends GraphNode> Either<org.openecomp.sdc.be.resources.data.CategoryData, StorageOperationStatus> getCategoryData(String name,
                                                                                                                                  NodeTypeEnum type,
                                                                                                                                  Class<T> clazz) {
        if (name != null) {
            String categoryUid = null;
            if (type == NodeTypeEnum.ResourceCategory) {
                String[] categoryFields = name.split("/");
                if (categoryFields.length != 2) {
                    return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
                }
                categoryUid = UniqueIdBuilder.buildResourceCategoryUid(categoryFields[0], categoryFields[1], type);
            } else {
                categoryUid = UniqueIdBuilder.buildServiceCategoryUid(name, type);
            }
            Either<T, JanusGraphOperationStatus> either = janusGraphGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(type), categoryUid, clazz);
            if (either.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = either.right().value();
                log.debug("Problem while geting category with id {}. reason - {}", categoryUid, janusGraphOperationStatus.name());
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
                } else {
                    return Either.right(StorageOperationStatus.GENERAL_ERROR);
                }
            }
            return Either.left((org.openecomp.sdc.be.resources.data.CategoryData) either.left().value());
        } else {
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
    }

    private List<Tag> convertToListOfTag(List<TagData> tagDataList) {
        List<Tag> tagList = new ArrayList<>();
        for (TagData elem : tagDataList) {
            Tag tag = new Tag();
            tag.setName(elem.getName());
            tagList.add(tag);
        }
        return tagList;
    }

    @Override
    public Either<List<PropertyScope>, ActionStatus> getAllPropertyScopes() {
        // Mock
        List<PropertyScope> propertyScopes = new ArrayList<>();
        PropertyScope propertyScope1 = new PropertyScope();
        propertyScope1.setName("A&AI");
        PropertyScope propertyScope2 = new PropertyScope();
        propertyScope2.setName("Order");
        PropertyScope propertyScope3 = new PropertyScope();
        propertyScope3.setName("Runtime");
        propertyScopes.add(propertyScope1);
        propertyScopes.add(propertyScope2);
        propertyScopes.add(propertyScope3);
        return Either.left(propertyScopes);
    }

    @Override
    public List<ArtifactType> getAllArtifactTypes() {
        final List<ArtifactConfiguration> artifactTypesList = ArtifactConfigManager.getInstance().getConfiguration();
        return artifactTypesList.stream().map(artifactConfiguration -> {
            final ArtifactType artifactType = new ArtifactType();
            artifactType.setName(artifactConfiguration.getType());
            return artifactType;
        }).collect(Collectors.toList());
    }

    @Override
    public Either<Configuration.HeatDeploymentArtifactTimeout, ActionStatus> getDefaultHeatTimeout() {
        return Either.left(getConfiguration().getHeatArtifactDeploymentTimeout());
    }

    @Override
    public Either<Map<String, String>, ActionStatus> getResourceTypesMap() {
        ResourceTypeEnum[] enumConstants = ResourceTypeEnum.class.getEnumConstants();
        Map<String, String> resourceTypes = new HashMap<>();
        if (enumConstants != null) {
            for (int i = 0; i < enumConstants.length; ++i) {
                resourceTypes.put(enumConstants[i].name(), enumConstants[i].getValue());
            }
        }
        return Either.left(resourceTypes);
    }

    @Override
    public <T extends GraphNode> Either<CategoryData, StorageOperationStatus> getNewCategoryData(String name, NodeTypeEnum type, Class<T> clazz) {
        if (name != null) {
            String categoryUid = UniqueIdBuilder.buildServiceCategoryUid(name, type);
            Map props = new HashMap<>();
            props.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normalizeCategoryName4Uniqueness(name));
            Either<List<T>, JanusGraphOperationStatus> either = janusGraphGenericDao.getByCriteria(type, props, clazz);
            if (either.isRight()) {
                JanusGraphOperationStatus janusGraphOperationStatus = either.right().value();
                log.debug("Problem while geting category with id {}. reason - {}", categoryUid, janusGraphOperationStatus.name());
                if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                    return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
                } else {
                    return Either.right(StorageOperationStatus.GENERAL_ERROR);
                }
            }
            return Either.left((CategoryData) either.left().value().get(0));
        } else {
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }
    }
}
