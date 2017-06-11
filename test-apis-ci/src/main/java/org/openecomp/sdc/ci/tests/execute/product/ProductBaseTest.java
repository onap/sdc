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

package org.openecomp.sdc.ci.tests.execute.product;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.PRODUCT_COMPONENT_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.BeforeMethod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public abstract class ProductBaseTest extends ComponentBaseTest {
	User productStrategistUser1;
	User productStrategistUser2;
	User productStrategistUser3;
	User productManager1;
	User productManager2;
	User adminUser;
	User designerUser;

	// Category1->Subcategory1->Grouping1
	protected List<CategoryDefinition> defaultCategories;

	protected static String auditAction;
	protected static ComponentOperationEnum operation;

	public ProductBaseTest(TestName testName, String className) {
		super(testName, className);
	}

	@BeforeMethod
	public void beforeProductTest() throws IOException, Exception {
		productStrategistUser1 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);
		productStrategistUser2 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST2);
		productStrategistUser3 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST3);
		productManager1 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		productManager2 = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER2);
		adminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		designerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		createDefaultChain();
	}

	private void createDefaultChain() throws Exception {
		CategoryDefinition productCategoryDefinition = ElementFactory.getDefaultCategory();
		SubCategoryDefinition productSubCategoryDefinition = ElementFactory.getDefaultSubCategory();
		GroupingDefinition productGroupingDefinition = ElementFactory.getDefaultGroup();
		productCategoryDefinition.addSubCategory(productSubCategoryDefinition);
		productSubCategoryDefinition.addGrouping(productGroupingDefinition);
		List<CategoryDefinition> definitionsList = new ArrayList<>();
		definitionsList.add(productCategoryDefinition);
		defaultCategories = createCategoriesChain(definitionsList);
	}

	private List<CategoryDefinition> createCategoriesChain(List<CategoryDefinition> categoryDefinitions)
			throws Exception {
		for (CategoryDefinition categoryDefinition : categoryDefinitions) {
			RestResponse createCategory = CategoryRestUtils.createCategory(categoryDefinition, productStrategistUser1,
					PRODUCT_COMPONENT_TYPE);
			int status = createCategory.getErrorCode().intValue();
			if (status == BaseRestUtils.STATUS_CODE_CREATED) {
				String categoryId = ResponseParser.getUniqueIdFromResponse(createCategory);
				categoryDefinition.setUniqueId(categoryId);
			}
			List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
			for (SubCategoryDefinition subCategoryDefinition : subcategories) {
				RestResponse createSubCategory = CategoryRestUtils.createSubCategory(subCategoryDefinition,
						categoryDefinition, productStrategistUser1, PRODUCT_COMPONENT_TYPE);
				status = createSubCategory.getErrorCode().intValue();
				if (status == BaseRestUtils.STATUS_CODE_CREATED) {
					String subCategoryId = ResponseParser.getUniqueIdFromResponse(createSubCategory);
					subCategoryDefinition.setUniqueId(subCategoryId);
				}
				List<GroupingDefinition> groupings = subCategoryDefinition.getGroupings();
				for (GroupingDefinition groupingDefinition : groupings) {
					RestResponse createGroupingRest = CategoryRestUtils.createGrouping(groupingDefinition,
							subCategoryDefinition, categoryDefinition, productStrategistUser1, PRODUCT_COMPONENT_TYPE);
					status = createGroupingRest.getErrorCode().intValue();
					if (status == BaseRestUtils.STATUS_CODE_CREATED) {
						String groupingId = ResponseParser.getUniqueIdFromResponse(createGroupingRest);
						groupingDefinition.setUniqueId(groupingId);
					}
				}
			}
		}
		RestResponse allCategories = CategoryRestUtils.getAllCategories(productStrategistUser1, PRODUCT_COMPONENT_TYPE);
		Gson gson = new Gson();
		List<CategoryDefinition> res = gson.fromJson(allCategories.getResponse(),
				new TypeToken<List<CategoryDefinition>>() {
				}.getType());
		return res;
	}

	// Category1->Subcategory1->[Grouping1, Grouping11]
	protected List<CategoryDefinition> addSecondGroupingToDefaultCategory() throws Exception {
		GroupingDefinition productGroupingDefinition = ElementFactory.getDefaultGroup();
		productGroupingDefinition.setName("Grouping11");
		defaultCategories.get(0).getSubcategories().get(0).addGrouping(productGroupingDefinition);
		return createCategoriesChain(defaultCategories);
	}

	// Category1->[Subcategory1->[Grouping1,
	// Grouping11],Subcategory2->[Grouping12]]
	protected List<CategoryDefinition> addSubcategoryAndGroupingToDefaultCategory() throws Exception {
		GroupingDefinition groupingDefinition1 = ElementFactory.getDefaultGroup();
		groupingDefinition1.setName("Grouping11");
		defaultCategories.get(0).getSubcategories().get(0).addGrouping(groupingDefinition1);

		SubCategoryDefinition subCategory2 = ElementFactory.getDefaultSubCategory();
		subCategory2.setName("Subcategory2");
		GroupingDefinition groupingDefinition2 = ElementFactory.getDefaultGroup();
		groupingDefinition2.setName("Grouping12");
		subCategory2.addGrouping(groupingDefinition2);
		defaultCategories.get(0).addSubCategory(subCategory2);
		return createCategoriesChain(defaultCategories);
	}

	// [Category1->[Subcategory1->[Grouping1,
	// Grouping11],Subcategory2->[Grouping12]],
	// Category2->[Subcategory1->[Grouping1],Subcategory2->[Grouping1]],
	// Category3->[Subcategory1->[Grouping11],Subcategory2->[Grouping11,
	// Grouping22]]]
	protected List<CategoryDefinition> addManyGroupingsDiffCategories() throws Exception {
		CategoryDefinition category2 = ElementFactory.getDefaultCategory();
		category2.setName("Category2");
		CategoryDefinition category3 = ElementFactory.getDefaultCategory();
		category3.setName("Category3");
		SubCategoryDefinition subCategory1 = ElementFactory.getDefaultSubCategory();
		subCategory1.setName("Subcategory1");
		SubCategoryDefinition subCategory2 = ElementFactory.getDefaultSubCategory();
		subCategory2.setName("Subcategory2");
		SubCategoryDefinition subCategory1_2 = ElementFactory.getDefaultSubCategory();
		subCategory1_2.setName("Subcategory1");
		SubCategoryDefinition subCategory2_2 = ElementFactory.getDefaultSubCategory();
		subCategory2_2.setName("Subcategory2");
		SubCategoryDefinition subCategory1_3 = ElementFactory.getDefaultSubCategory();
		subCategory1_3.setName("Subcategory1");
		SubCategoryDefinition subCategory2_3 = ElementFactory.getDefaultSubCategory();
		subCategory2_3.setName("Subcategory2");

		GroupingDefinition groupingDefinition1 = ElementFactory.getDefaultGroup();
		groupingDefinition1.setName("Grouping1");
		GroupingDefinition groupingDefinition11 = ElementFactory.getDefaultGroup();
		groupingDefinition11.setName("Grouping11");
		GroupingDefinition groupingDefinition12 = ElementFactory.getDefaultGroup();
		groupingDefinition12.setName("Grouping12");
		GroupingDefinition groupingDefinition22 = ElementFactory.getDefaultGroup();
		groupingDefinition22.setName("Grouping22");

		defaultCategories.get(0).getSubcategories().get(0).addGrouping(groupingDefinition11);
		subCategory2.addGrouping(groupingDefinition12);
		defaultCategories.get(0).addSubCategory(subCategory2);

		defaultCategories.add(category2);
		defaultCategories.add(category3);
		category2.addSubCategory(subCategory1_2);
		category2.addSubCategory(subCategory2_2);
		subCategory1_2.addGrouping(groupingDefinition1);
		subCategory2_2.addGrouping(groupingDefinition1);
		category3.addSubCategory(subCategory1_3);
		category3.addSubCategory(subCategory2_3);
		subCategory1_3.addGrouping(groupingDefinition11);
		subCategory2_3.addGrouping(groupingDefinition11);
		subCategory2_3.addGrouping(groupingDefinition22);
		return createCategoriesChain(defaultCategories);
	}
}
