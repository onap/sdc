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

package org.openecomp.sdc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Category;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.CategoryData;

import fj.data.Either;

public class ElementOperationMock implements IElementOperation {

	CategoryDefinition resourceCategory;
	CategoryDefinition serviceCategory;
	CategoryDefinition productCategory;

	Category oldService;

	public ElementOperationMock() {
		resourceCategory = new CategoryDefinition();
		resourceCategory.setName("Network Layer 2-3");
		SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition();
		subCategoryDefinition.setName("Router");
		SubCategoryDefinition subCategoryDefinition1 = new SubCategoryDefinition();
		subCategoryDefinition1.setName("Gateway");

		resourceCategory.addSubCategory(subCategoryDefinition);
		resourceCategory.addSubCategory(subCategoryDefinition1);

		serviceCategory = new CategoryDefinition();
		serviceCategory.setName("Mobility");
		oldService = new Category();
		oldService.setName("Mobility");

		productCategory = new CategoryDefinition();
		productCategory.setName("Network Layer 2-31");
		SubCategoryDefinition subCategoryDefinition11 = new SubCategoryDefinition();
		subCategoryDefinition11.setName("Router1");
		GroupingDefinition group = new GroupingDefinition();
		group.setName("group1");
		subCategoryDefinition11.addGrouping(group);
		productCategory.addSubCategory(subCategoryDefinition11);

	}

	@Override
	public Either<List<CategoryDefinition>, ActionStatus> getAllResourceCategories() {

		List<CategoryDefinition> categories = new ArrayList<CategoryDefinition>();
		categories.add(resourceCategory);
		return Either.left(categories);

	}

	@Override
	public Either<List<CategoryDefinition>, ActionStatus> getAllServiceCategories() {

		List<CategoryDefinition> categories = new ArrayList<CategoryDefinition>();
		categories.add(serviceCategory);
		return Either.left(categories);

	}

	/*
	 * @Override public Either<Category, ActionStatus> getCategory(String name) { if (name.equals(resourceCategory.getName())){ return Either.left(resourceCategory); } else { return Either.right(ActionStatus.CATEGORY_NOT_FOUND); } }
	 */

	@Override
	public Either<List<Tag>, ActionStatus> getAllTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<List<PropertyScope>, ActionStatus> getAllPropertyScopes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<List<ArtifactType>, ActionStatus> getAllArtifactTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<Map<String, Object>, ActionStatus> getAllDeploymentArtifactTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends GraphNode> Either<CategoryData, StorageOperationStatus> getCategoryData(String name, NodeTypeEnum type, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<Integer, ActionStatus> getDefaultHeatTimeout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<CategoryDefinition, ActionStatus> createCategory(CategoryDefinition category, NodeTypeEnum nodeType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<CategoryDefinition, ActionStatus> deleteCategory(NodeTypeEnum nodeType, String categoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<Boolean, ActionStatus> isCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<List<CategoryDefinition>, ActionStatus> getAllCategories(NodeTypeEnum nodeType, boolean inTransaction) {

		List<CategoryDefinition> categories = new ArrayList<CategoryDefinition>();
		switch (nodeType) {
		case ResourceNewCategory:
			categories.add(resourceCategory);
			break;
		case ProductCategory:
			categories.add(productCategory);
			break;
		case ServiceNewCategory:
			categories.add(serviceCategory);
			break;
		default:
			break;
		}
		return Either.left(categories);
	}

	@Override
	public Either<CategoryDefinition, ActionStatus> getCategory(NodeTypeEnum nodeType, String categoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> getSubCategoryUniqueForType(NodeTypeEnum nodeType, String normalizedName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<Boolean, ActionStatus> isSubCategoryUniqueForCategory(NodeTypeEnum nodeType, String subCategoryNormName, String parentCategoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> deleteSubCategory(NodeTypeEnum nodeType, String subCategoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<GroupingDefinition, ActionStatus> createGrouping(String subCategoryId, GroupingDefinition grouping, NodeTypeEnum nodeType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<GroupingDefinition, ActionStatus> deleteGrouping(NodeTypeEnum nodeType, String groupingId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> getSubCategory(NodeTypeEnum nodeType, String subCategoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<Boolean, ActionStatus> isGroupingUniqueForSubCategory(NodeTypeEnum nodeType, String groupingNormName, String parentSubCategoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<GroupingDefinition, ActionStatus> getGroupingUniqueForType(NodeTypeEnum nodeType, String groupingNormalizedName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<Map<String, String>, ActionStatus> getResourceTypesMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends GraphNode> Either<org.openecomp.sdc.be.resources.data.category.CategoryData, StorageOperationStatus> getNewCategoryData(String name, NodeTypeEnum type, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<List<CategoryDefinition>, ActionStatus> getAllProductCategories() {
		List<CategoryDefinition> categories = new ArrayList<CategoryDefinition>();
		categories.add(productCategory);
		return Either.left(categories);
	}

	@Override
	public Either<CategoryDefinition, ActionStatus> createCategory(CategoryDefinition category, NodeTypeEnum nodeType, boolean inTransaction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<SubCategoryDefinition, ActionStatus> createSubCategory(String categoryId, SubCategoryDefinition subCategory, NodeTypeEnum nodeType, boolean inTransaction) {
		// TODO Auto-generated method stub
		return null;
	}

}
