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

package org.openecomp.sdc.ci.tests.datatypes;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;

public abstract class ComponentReqDetails {

	protected String name;
	protected String description;
	protected List<String> tags = new ArrayList<>();
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

	public String getCsarVersion() {
		return csarVersion;
	}

	public void setCsarVersion(String csarVersion) {
		this.csarVersion = csarVersion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		tags.add(name);
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	// public String getCategory() {
	// return category;
	// }
	//
	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}

	public void setCreatorFullName(String creatorFullName) {
		this.creatorFullName = creatorFullName;
	}

	public void setLastUpdaterUserId(String lastUpdaterUserId) {
		this.lastUpdaterUserId = lastUpdaterUserId;
	}

	public void setLastUpdaterFullName(String lastUpdaterFullName) {
		this.lastUpdaterFullName = lastUpdaterFullName;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	public void setLastUpdateDate(Long lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public void setLifecycleState(LifecycleStateEnum lifecycleState) {
		this.lifecycleState = lifecycleState;
	}

	public void setUUID(String uUID) {
		this.UUID = uUID;
	}

	public String getCreatorUserId() {
		return creatorUserId;
	}

	public String getCreatorFullName() {
		return creatorFullName;
	}

	public String getLastUpdaterUserId() {
		return lastUpdaterUserId;
	}

	public String getLastUpdaterFullName() {
		return lastUpdaterFullName;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public Long getLastUpdateDate() {
		return lastUpdateDate;
	}

	public LifecycleStateEnum getLifecycleState() {
		return lifecycleState;
	}

	public String getUUID() {
		return UUID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}

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

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	public String getCsarUUID() {
		return csarUUID;
	}

	public void setCsarUUID(String csarUUID) {
		this.csarUUID = csarUUID;
	}

	public String getImportedToscaChecksum() {
		return importedToscaChecksum;
	}

	public void setImportedToscaChecksum(String importedToscaChecksum) {
		this.importedToscaChecksum = importedToscaChecksum;
	}

	public String getInvariantUUID() {
		return invariantUUID;
	}

	public void setInvariantUUID(String invariantUUID) {
		this.invariantUUID = invariantUUID;
	}

}
