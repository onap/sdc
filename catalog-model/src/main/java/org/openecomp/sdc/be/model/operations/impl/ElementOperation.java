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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.category.SubCategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
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
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanGraph;
//import com.tinkerpop.blueprints.Vertex;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("element-operation")
public class ElementOperation implements IElementOperation {

	private TitanGenericDao titanGenericDao;

	public ElementOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
		super();
		this.titanGenericDao = titanGenericDao;
	}

	private static Logger log = LoggerFactory.getLogger(ElementOperation.class.getName());

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
	 * 
	 */

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
			Either<CategoryData, TitanOperationStatus> createNode = titanGenericDao.createNode(categoryData, CategoryData.class);
			if (createNode.isRight()) {
				TitanOperationStatus value = createNode.right().value();
				ActionStatus actionStatus = ActionStatus.GENERAL_ERROR;
				log.debug("Problem while creating category, reason {}", value);
				if (value == TitanOperationStatus.TITAN_SCHEMA_VIOLATION) {
					actionStatus = ActionStatus.COMPONENT_CATEGORY_ALREADY_EXISTS;
				}
				result = Either.right(actionStatus);
				return result;
			}
			CategoryDefinition created = new CategoryDefinition(createNode.left().value().getCategoryDataDefinition());
			result = Either.left(created);
			return result;
		} finally {
			if (inTransaction == false) {
				if (result != null && result.isLeft()) {
					titanGenericDao.commit();
				} else {
					titanGenericDao.rollback();
				}
			}
		}
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType) {
		return createSubCategory(categoryId, subCategory, nodeType, false);
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType, boolean inTransaction) {

		Either<SubCategoryDefinition, ActionStatus> result = null;

		try {
			// create edge from category to sub-category
			Either<CategoryData, TitanOperationStatus> categoryNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId, CategoryData.class);
			ActionStatus actionStatus = ActionStatus.GENERAL_ERROR;
			if (categoryNode.isRight()) {
				TitanOperationStatus titanOperationStatus = categoryNode.right().value();
				log.debug("Problem while fetching category, reason {}", titanOperationStatus);
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
					actionStatus = ActionStatus.COMPONENT_CATEGORY_NOT_FOUND;
				}
				result = Either.right(actionStatus);
				return result;
			}

			CategoryDataDefinition categoryDataDefinition = categoryNode.left().value().getCategoryDataDefinition();
			subCategory.setUniqueId(UniqueIdBuilder.buildSubCategoryUid(categoryDataDefinition.getUniqueId(), subCategory.getNormalizedName()));
			SubCategoryData subCategoryData = new SubCategoryData(nodeType, subCategory);

			Either<SubCategoryData, TitanOperationStatus> subCategoryNode = titanGenericDao.createNode(subCategoryData, SubCategoryData.class);
			if (subCategoryNode.isRight()) {
				TitanOperationStatus titanOperationStatus = subCategoryNode.right().value();
				log.debug("Problem while creating category, reason {}", titanOperationStatus);
				if (titanOperationStatus == TitanOperationStatus.TITAN_SCHEMA_VIOLATION) {
					actionStatus = ActionStatus.COMPONENT_SUB_CATEGORY_EXISTS_FOR_CATEGORY;
				}
				result = Either.right(actionStatus);
				return result;
			}

			Either<GraphRelation, TitanOperationStatus> relation = titanGenericDao.createRelation(categoryNode.left().value(), subCategoryNode.left().value(), GraphEdgeLabels.SUB_CATEGORY, null);
			if (relation.isRight()) {
				log.debug("Problem while create relation between category and sub-category ", relation.right().value());
				result = Either.right(actionStatus);
				return result;
			}
			SubCategoryDefinition subCategoryCreated = new SubCategoryDefinition(subCategoryNode.left().value().getSubCategoryDataDefinition());
			result = Either.left(subCategoryCreated);
			return result;
		} finally {
			if (inTransaction == false) {
				if (result != null && result.isLeft()) {
					titanGenericDao.commit();
				} else {
					titanGenericDao.rollback();
				}
			}
		}
	}

	@Override
	public Either<GroupingDefinition, ActionStatus> createGrouping(String subCategoryId, GroupingDefinition grouping, NodeTypeEnum nodeType) {

		Either<GroupingDefinition, ActionStatus> result = null;

		try {
			// create edge from sub-category to grouping
			Either<SubCategoryData, TitanOperationStatus> subCategoryNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
			ActionStatus actionStatus = ActionStatus.GENERAL_ERROR;
			if (subCategoryNode.isRight()) {
				TitanOperationStatus titanOperationStatus = subCategoryNode.right().value();
				log.debug("Problem while fetching category, reason {}", titanOperationStatus);
				if (titanOperationStatus == TitanOperationStatus.TITAN_SCHEMA_VIOLATION) {
					actionStatus = ActionStatus.COMPONENT_CATEGORY_NOT_FOUND;
				}
				result = Either.right(actionStatus);
				return result;
			}

			SubCategoryDataDefinition subCatData = subCategoryNode.left().value().getSubCategoryDataDefinition();
			grouping.setUniqueId(UniqueIdBuilder.buildGroupingUid(subCatData.getUniqueId(), grouping.getNormalizedName()));
			GroupingData groupingData = new GroupingData(nodeType, grouping);

			Either<GroupingData, TitanOperationStatus> groupingNode = titanGenericDao.createNode(groupingData, GroupingData.class);
			if (groupingNode.isRight()) {
				TitanOperationStatus titanOperationStatus = groupingNode.right().value();
				log.debug("Problem while creating grouping, reason {}", titanOperationStatus);
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
					actionStatus = ActionStatus.COMPONENT_GROUPING_EXISTS_FOR_SUB_CATEGORY;
				}
				result = Either.right(actionStatus);
				return result;
			}

			Either<GraphRelation, TitanOperationStatus> relation = titanGenericDao.createRelation(subCategoryNode.left().value(), groupingNode.left().value(), GraphEdgeLabels.GROUPING, null);
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
				titanGenericDao.commit();
			} else {
				titanGenericDao.rollback();
			}
		}
	}

	@Override
	public Either<List<CategoryDefinition>, ActionStatus> getAllCategories(NodeTypeEnum nodeType, boolean inTransaction) {
		try {
			if (nodeType != NodeTypeEnum.ResourceNewCategory && nodeType != NodeTypeEnum.ServiceNewCategory && nodeType != NodeTypeEnum.ProductCategory) {
				log.debug("Unknown category type {}", nodeType.name());
				return Either.right(ActionStatus.GENERAL_ERROR);
			}

			Either<List<org.openecomp.sdc.be.resources.data.category.CategoryData>, TitanOperationStatus> either = titanGenericDao.getAll(nodeType, org.openecomp.sdc.be.resources.data.category.CategoryData.class);
			if (either.isRight() && (either.right().value() != TitanOperationStatus.NOT_FOUND)) {
				log.debug("Problem while get all categories. reason - {}", either.right().value());
				return Either.right(ActionStatus.GENERAL_ERROR);
			}
			List<CategoryData> categoryDataList = either.isLeft() ? either.left().value() : null;
			List<CategoryDefinition> categoryList = new ArrayList<CategoryDefinition>();
			if (categoryDataList != null) {
				for (CategoryData elem : categoryDataList) {
					CategoryDataDefinition categoryDataDefinition = elem.getCategoryDataDefinition();

					CategoryDefinition categoryDefinition = new CategoryDefinition(categoryDataDefinition);
					String categoryName = categoryDataDefinition.getName();
					log.trace("Found category {}, category type {}", categoryName, nodeType);
					TitanOperationStatus setSubCategories = setSubCategories(nodeType, categoryDefinition);
					if (setSubCategories != TitanOperationStatus.OK) {
						log.debug("Failed to set sub-categories for category {}, category type {}, error {}", categoryName, nodeType, setSubCategories);
						return Either.right(ActionStatus.GENERAL_ERROR);
					}
					categoryList.add(categoryDefinition);
				}
			}
			return Either.left(categoryList);
		} finally {
			if (!inTransaction) {
				titanGenericDao.commit();
			}
		}
	}

	private TitanOperationStatus setSubCategories(NodeTypeEnum parentNodeType, CategoryDefinition parentCategory) {
		NodeTypeEnum childNodeType = getChildNodeType(parentNodeType);
		if (childNodeType != null) {
			String categoryName = parentCategory.getName();
			log.trace("Getting sub-categories for category {}, category type {}", categoryName, parentNodeType);
			Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, TitanOperationStatus> parentNode = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(parentNodeType), parentCategory.getUniqueId(), GraphEdgeLabels.SUB_CATEGORY,
					childNodeType, SubCategoryData.class);
			if (parentNode.isRight()) {
				TitanOperationStatus titanOperationStatus = parentNode.right().value();
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
					log.trace("Didn't find subcategories for category {}, category type {}", categoryName, parentNodeType);
					titanOperationStatus = TitanOperationStatus.OK;
				}
				return titanOperationStatus;
			}
			List<ImmutablePair<SubCategoryData, GraphEdge>> subsCategoriesData = parentNode.left().value();
			List<SubCategoryDefinition> subCategoriesDefinitions = new ArrayList<>();
			for (ImmutablePair<SubCategoryData, GraphEdge> subCatPair : subsCategoriesData) {
				SubCategoryDataDefinition subCategoryDataDefinition = subCatPair.getLeft().getSubCategoryDataDefinition();
				SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition(subCategoryDataDefinition);

				log.trace("Found sub-category {} for category {}, category type {}", subCategoryDataDefinition.getName(), categoryName, parentNodeType);
				TitanOperationStatus setGroupings = setGroupings(childNodeType, subCategoryDefinition);
				if (setGroupings != TitanOperationStatus.OK) {
					log.debug("Failed to set groupings for sub-category {}, sub-category type {}, error {}", subCategoryDataDefinition.getName(), childNodeType, setGroupings);
					return TitanOperationStatus.GENERAL_ERROR;
				}
				subCategoriesDefinitions.add(subCategoryDefinition);
			}
			parentCategory.setSubcategories(subCategoriesDefinitions);
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setGroupings(NodeTypeEnum parentNodeType, SubCategoryDefinition parentSubCategory) {
		NodeTypeEnum childNodeType = getChildNodeType(parentNodeType);
		if (childNodeType != null) {
			String subCategoryName = parentSubCategory.getName();
			log.trace("Getting groupings for subcategory {}, subcategory type {}", subCategoryName, parentNodeType);
			Either<List<ImmutablePair<GroupingData, GraphEdge>>, TitanOperationStatus> parentNode = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(parentNodeType), parentSubCategory.getUniqueId(), GraphEdgeLabels.GROUPING,
					childNodeType, GroupingData.class);
			if (parentNode.isRight()) {
				TitanOperationStatus titanOperationStatus = parentNode.right().value();
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
					log.trace("Didn't find groupings for subcategory {}, subcategory type {}", subCategoryName, parentNodeType);
					titanOperationStatus = TitanOperationStatus.OK;
				}
				return titanOperationStatus;
			}
			List<ImmutablePair<GroupingData, GraphEdge>> groupingData = parentNode.left().value();
			List<GroupingDefinition> groupingDefinitions = new ArrayList<>();
			for (ImmutablePair<GroupingData, GraphEdge> groupPair : groupingData) {
				GroupingDataDefinition groupingDataDefinition = groupPair.getLeft().getGroupingDataDefinition();
				log.trace("Found grouping {} for sub-category {}, sub-category type {}", groupingDataDefinition.getName(), subCategoryName, parentNodeType);
				groupingDefinitions.add(new GroupingDefinition(groupingDataDefinition));
			}
			parentSubCategory.setGroupings(groupingDefinitions);
		}
		return TitanOperationStatus.OK;
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

	@Override
	public Either<CategoryDefinition, ActionStatus> getCategory(NodeTypeEnum nodeType, String categoryId) {
		try {
			if (nodeType != NodeTypeEnum.ResourceNewCategory && nodeType != NodeTypeEnum.ServiceNewCategory && nodeType != NodeTypeEnum.ProductCategory) {
				log.debug("Unknown category type {}", nodeType.name());
				return Either.right(ActionStatus.GENERAL_ERROR);
			}

			Either<CategoryData, TitanOperationStatus> categoryDataEither = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId, CategoryData.class);
			if (categoryDataEither.isRight()) {
				TitanOperationStatus titanOperationStatus = categoryDataEither.right().value();
				log.debug("Problem while get category by id {}. reason {}", categoryId, titanOperationStatus);
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
					return Either.right(ActionStatus.COMPONENT_CATEGORY_NOT_FOUND);
				}
				return Either.right(ActionStatus.GENERAL_ERROR);
			}
			CategoryDataDefinition categoryDataDefinition = categoryDataEither.left().value().getCategoryDataDefinition();
			return Either.left(new CategoryDefinition(categoryDataDefinition));
		} finally {
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> getSubCategory(NodeTypeEnum nodeType, String subCategoryId) {
		try {
			if (nodeType != NodeTypeEnum.ResourceSubcategory && nodeType != NodeTypeEnum.ProductSubcategory) {
				log.debug("Unknown sub-category type {}", nodeType.name());
				return Either.right(ActionStatus.GENERAL_ERROR);
			}

			Either<SubCategoryData, TitanOperationStatus> subCategoryDataEither = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
			if (subCategoryDataEither.isRight()) {
				TitanOperationStatus titanOperationStatus = subCategoryDataEither.right().value();
				log.debug("Problem while get sub-category by id {}. reason {}", subCategoryId, titanOperationStatus);
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
					return Either.right(ActionStatus.COMPONENT_CATEGORY_NOT_FOUND);
				}
				return Either.right(ActionStatus.GENERAL_ERROR);
			}
			SubCategoryDataDefinition subCategoryDataDefinition = subCategoryDataEither.left().value().getSubCategoryDataDefinition();
			return Either.left(new SubCategoryDefinition(subCategoryDataDefinition));
		} finally {
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<CategoryDefinition, ActionStatus> deleteCategory(NodeTypeEnum nodeType, String categoryId) {
		Either<CategoryDefinition, ActionStatus> result = null;
		try {
			if (nodeType != NodeTypeEnum.ResourceNewCategory && nodeType != NodeTypeEnum.ServiceNewCategory && nodeType != NodeTypeEnum.ProductCategory) {
				log.debug("Unknown category type {}", nodeType.name());
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}
			Either<CategoryData, TitanOperationStatus> categoryDataEither = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId, CategoryData.class);
			if (categoryDataEither.isRight()) {
				log.debug("Failed to retrieve  category for id {} ", categoryId);
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}

			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.debug("Couldn't fetch titan graph");
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}

			TitanGraph tGraph = graph.left().value();

			Iterable<TitanVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), categoryId).vertices();
			Iterator<TitanVertex> iterator = verticesArtifact.iterator();
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
				titanGenericDao.commit();
			} else {
				titanGenericDao.rollback();
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
			Either<SubCategoryData, TitanOperationStatus> subCategoryDataEither = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
			if (subCategoryDataEither.isRight()) {
				log.debug("Failed to retrieve  sub-category for id {}", subCategoryId);
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}

			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.debug("Couldn't fetch titan graph");
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}

			TitanGraph tGraph = graph.left().value();

			Iterable<TitanVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId).vertices();
			Iterator<TitanVertex> iterator = verticesArtifact.iterator();
			if (!iterator.hasNext()) {
				log.debug("No sub-category node for id {}", subCategoryId);
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}
			Vertex artifactV = iterator.next();
			artifactV.remove();
			;
			SubCategoryDefinition deleted = new SubCategoryDefinition(subCategoryDataEither.left().value().getSubCategoryDataDefinition());
			result = Either.left(deleted);
			return result;
		} finally {
			if (result != null && result.isLeft()) {
				titanGenericDao.commit();
			} else {
				titanGenericDao.rollback();
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
			Either<GroupingData, TitanOperationStatus> groupingDataEither = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), groupingId, GroupingData.class);
			if (groupingDataEither.isRight()) {
				log.debug("Failed to retrieve  grouping for id {}", groupingId);
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}

			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.debug("Couldn't fetch titan graph");
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}

			TitanGraph tGraph = graph.left().value();

			Iterable<TitanVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(nodeType), groupingId).vertices();
			Iterator<TitanVertex> iterator = verticesArtifact.iterator();
			if (!iterator.hasNext()) {
				log.debug("No grouping node for id {}", groupingId);
				result = Either.right(ActionStatus.GENERAL_ERROR);
				return result;
			}
			Vertex artifactV = iterator.next();
			artifactV.remove();
			;
			GroupingDefinition deleted = new GroupingDefinition(groupingDataEither.left().value().getGroupingDataDefinition());
			result = Either.left(deleted);
			return result;
		} finally {
			if (result != null && result.isLeft()) {
				titanGenericDao.commit();
			} else {
				titanGenericDao.rollback();
			}
		}
	}

	@Override
	public Either<Boolean, ActionStatus> isCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName) {

		Map<String, Object> properties = new HashMap<>();
		properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), normalizedName);
		try {
			Either<List<CategoryData>, TitanOperationStatus> categoryEither = titanGenericDao.getByCriteria(nodeType, properties, CategoryData.class);
			if (categoryEither.isRight() && categoryEither.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.debug("Failed to get categories, nodeType {}, normalizedName {}, error {}", nodeType, normalizedName, categoryEither.right().value());
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
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<Boolean, ActionStatus> isSubCategoryUniqueForCategory(NodeTypeEnum nodeType, String subCategoryNormName, String parentCategoryId) {

		String subCategoryId = UniqueIdBuilder.buildSubCategoryUid(parentCategoryId, subCategoryNormName);
		try {
			Either<SubCategoryData, TitanOperationStatus> subCategoryDataEither = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), subCategoryId, SubCategoryData.class);
			if (subCategoryDataEither.isRight() && subCategoryDataEither.right().value() != TitanOperationStatus.NOT_FOUND) {
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
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<Boolean, ActionStatus> isGroupingUniqueForSubCategory(NodeTypeEnum nodeType, String groupingNormName, String parentSubCategoryId) {

		String groupingId = UniqueIdBuilder.buildGroupingUid(parentSubCategoryId, groupingNormName);
		try {
			Either<GroupingData, TitanOperationStatus> groupingDataEither = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), groupingId, GroupingData.class);
			if (groupingDataEither.isRight() && groupingDataEither.right().value() != TitanOperationStatus.NOT_FOUND) {
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
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> getSubCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), normalizedName);
		try {
			Either<List<SubCategoryData>, TitanOperationStatus> subCategoryEither = titanGenericDao.getByCriteria(nodeType, properties, SubCategoryData.class);
			if (subCategoryEither.isRight() && subCategoryEither.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.debug("Failed to get sub-categories, nodeType {}, normalizedName {}, error {}", nodeType, normalizedName, subCategoryEither.right().value());
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
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<GroupingDefinition, ActionStatus> getGroupingUniqueForType(NodeTypeEnum nodeType, String groupingNormalizedName) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), groupingNormalizedName);
		try {
			Either<List<GroupingData>, TitanOperationStatus> groupingEither = titanGenericDao.getByCriteria(nodeType, properties, GroupingData.class);
			if (groupingEither.isRight() && groupingEither.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.debug("Failed to get grouping, nodeType {}, normalizedName {}, error {}", nodeType, groupingNormalizedName, groupingEither.right().value());
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
			titanGenericDao.commit();
		}
	}

	/*
	 *
	 */

	@Override
	public Either<List<Tag>, ActionStatus> getAllTags() {
		try {
			Either<List<TagData>, TitanOperationStatus> either = titanGenericDao.getAll(NodeTypeEnum.Tag, TagData.class);
			if (either.isRight()) {
				log.debug("Problem while get all tags. reason - {}", either.right().value());
				return Either.right(ActionStatus.GENERAL_ERROR);
			}
			List<TagData> tagDataList = either.left().value();
			List<Tag> tagList = convertToListOfTag(tagDataList);
			return Either.left(tagList);
		} finally {
			titanGenericDao.commit();
		}
	}

	@Override
	public <T extends GraphNode> Either<org.openecomp.sdc.be.resources.data.CategoryData, StorageOperationStatus> getCategoryData(String name, NodeTypeEnum type, Class<T> clazz) {
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
			Either<T, TitanOperationStatus> either = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(type), categoryUid, clazz);

			if (either.isRight()) {
				TitanOperationStatus titanOperationStatus = either.right().value();
				log.debug("Problem while geting category with id {}. reason - {}", categoryUid, titanOperationStatus.name());
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
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
		List<Tag> tagList = new ArrayList<Tag>();
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
		List<PropertyScope> propertyScopes = new ArrayList<PropertyScope>();
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
	public Either<List<ArtifactType>, ActionStatus> getAllArtifactTypes() {
		List<ArtifactType> artifactTypes = new ArrayList<ArtifactType>();

		List<String> artifactTypesList = ConfigurationManager.getConfigurationManager().getConfiguration().getArtifactTypes();
		for (String artifactType : artifactTypesList) {
			ArtifactType artifactT = new ArtifactType();
			artifactT.setName(artifactType);
			artifactTypes.add(artifactT);
		}
		return Either.left(artifactTypes);
	}

	@Override
	public Either<Map<String, Object>, ActionStatus> getAllDeploymentArtifactTypes() {

		Map<String, Object> artifactTypes = new HashMap<String, Object>();
		Map<String, ArtifactTypeConfig> artifactResourceTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceDeploymentArtifacts();
		Map<String, ArtifactTypeConfig> artifactServiceTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getServiceDeploymentArtifacts();
		Map<String, ArtifactTypeConfig> artifactResourceInstanceTypes = ConfigurationManager.getConfigurationManager().getConfiguration().getResourceInstanceDeploymentArtifacts();

		artifactTypes.put("resourceDeploymentArtifacts", artifactResourceTypes);
		artifactTypes.put("serviceDeploymentArtifacts", artifactServiceTypes);
		artifactTypes.put("resourceInstanceDeploymentArtifacts", artifactResourceInstanceTypes);

		return Either.left(artifactTypes);

	}

	@Override
	public Either<Integer, ActionStatus> getDefaultHeatTimeout() {
		return Either.left(ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultHeatArtifactTimeoutMinutes());
	}

	@Override
	public Either<Map<String, String>, ActionStatus> getResourceTypesMap() {
		ResourceTypeEnum[] enumConstants = ResourceTypeEnum.class.getEnumConstants();
		Map<String, String> resourceTypes = new HashMap<String, String>();
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
			Either<List<T>, TitanOperationStatus> either = titanGenericDao.getByCriteria(type, props, clazz);

			if (either.isRight()) {
				TitanOperationStatus titanOperationStatus = either.right().value();
				log.debug("Problem while geting category with id {}. reason - {}", categoryUid, titanOperationStatus.name());
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
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
