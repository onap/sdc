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

package org.onap.sdc.backend.ci.tests.datatypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class ComponentReqDetails {

	@Setter(AccessLevel.NONE)
	protected String name;
	protected String description;
	protected List<String> tags = new ArrayList<>();
	protected List<String> models = new ArrayList<>();
	protected String contactId;
	protected String icon;
	protected String uniqueId;
	protected String creatorUserId;
	protected String creatorFullName;
	protected String lastUpdaterUserId;
	protected String lastUpdaterFullName;
	protected Long creationDate;
	protected Long lastUpdateDate;
	protected LifecycleStateEnum lifecycleState;
	protected String version;
	protected String UUID;
	protected List<CategoryDefinition> categories;
	protected String projectCode;
	protected String csarUUID;
	protected String csarVersion;
	protected String importedToscaChecksum;
	protected String invariantUUID;

	public void setName(String name) {
		this.name = name;
		tags.add(name);
	}

	// public String getCategory() {
	// return category;
	// }
	//

	public void removeAllCategories() {
		this.categories = new ArrayList<>();
	}

	public void addCategoryChain(String category, String subCategory) {
		if (category != null || subCategory != null) {
			if (categories == null) {
				categories = new ArrayList<>();
			}
			CategoryDefinition selectedCategory = null;
			for (CategoryDefinition categoryDef : categories) {
				if (categoryDef.getName().equals(category)) {
					selectedCategory = categoryDef;
				}
			}
			if (selectedCategory == null) {
				selectedCategory = new CategoryDefinition();
				selectedCategory.setName(category);
				categories.add(selectedCategory);
			}
			if (subCategory != null) {
				List<SubCategoryDefinition> subcategories = selectedCategory.getSubcategories();
				if (subcategories == null) {
					subcategories = new ArrayList<>();
					selectedCategory.setSubcategories(subcategories);
				}
				SubCategoryDefinition selectedSubcategory = null;
				for (SubCategoryDefinition subcategory : subcategories) {
					if (subcategory.getName().equals(subCategory)) {
						selectedSubcategory = subcategory;
					}
				}
				if (selectedSubcategory == null) {
					selectedSubcategory = new SubCategoryDefinition();
					selectedSubcategory.setName(subCategory);
					subcategories.add(selectedSubcategory);
				}
			}
		}
	}

	public void addCategory(String category) {
		addCategoryChain(category, null);
	}

}
