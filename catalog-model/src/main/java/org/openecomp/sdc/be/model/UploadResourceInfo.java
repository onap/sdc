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

package org.openecomp.sdc.be.model;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.common.api.UploadArtifactInfo;

public class UploadResourceInfo {

	public UploadResourceInfo(String payload, String payloadName, String description, String category,
			List<String> tags, List<UploadArtifactInfo> artifactsList) {
		super();
		this.payloadData = payload;
		this.payloadName = payloadName;
		this.description = description;
		// this.category = category;
		this.tags = tags;
		this.artifactList = artifactsList;
		if (category != null) {
			String[] arr = category.split("/");
			if (arr.length >= 2) {
				categories = new ArrayList<>();
				CategoryDefinition catDef = new CategoryDefinition();
				catDef.setName(arr[0]);
				SubCategoryDefinition subCat = new SubCategoryDefinition();
				subCat.setName(arr[1]);
				catDef.addSubCategory(subCat);
				categories.add(catDef);
			}
		}
	}

	public UploadResourceInfo() {
	}

	private String payloadData;
	private String payloadName;
	private String description;
	// private String category;
	private List<String> tags;
	private List<CategoryDefinition> categories;

	private List<UploadArtifactInfo> artifactList;
	private String contactId, name, resourceIconPath, icon, vendorName, vendorRelease;

	private String resourceType = "VFC";

	public String getPayloadData() {
		return payloadData;
	}

	public void setPayloadData(String payload) {
		this.payloadData = payload;
	}

	public String getPayloadName() {
		return payloadName;
	}

	public void setPayloadName(String payloadName) {
		this.payloadName = payloadName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// public String getCategory() {
	// return category;
	// }
	// public void setCategory(String category) {
	// this.category = category;
	// }
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<UploadArtifactInfo> getArtifactList() {
		return artifactList;
	}

	public void setArtifactList(List<UploadArtifactInfo> artifactsList) {
		this.artifactList = artifactsList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactList == null) ? 0 : artifactList.hashCode());
		result = prime * result + ((contactId == null) ? 0 : contactId.hashCode());
		// result = prime * result + ((category == null) ? 0 :
		// category.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((payloadData == null) ? 0 : payloadData.hashCode());
		result = prime * result + ((payloadName == null) ? 0 : payloadName.hashCode());
		result = prime * result + ((resourceIconPath == null) ? 0 : resourceIconPath.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
		result = prime * result + ((vendorRelease == null) ? 0 : vendorRelease.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UploadResourceInfo other = (UploadResourceInfo) obj;
		if (artifactList == null) {
			if (other.artifactList != null)
				return false;
		} else if (!artifactList.equals(other.artifactList))
			return false;
		if (contactId == null) {
			if (other.contactId != null)
				return false;
		} else if (!contactId.equals(other.contactId))
			return false;
		// if (category == null) {
		// if (other.category != null)
		// return false;
		// } else if (!category.equals(other.category))
		// return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (icon == null) {
			if (other.icon != null)
				return false;
		} else if (!icon.equals(other.icon))
			return false;
		if (payloadData == null) {
			if (other.payloadData != null)
				return false;
		} else if (!payloadData.equals(other.payloadData))
			return false;
		if (payloadName == null) {
			if (other.payloadName != null)
				return false;
		} else if (!payloadName.equals(other.payloadName))
			return false;
		if (resourceIconPath == null) {
			if (other.resourceIconPath != null)
				return false;
		} else if (!resourceIconPath.equals(other.resourceIconPath))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (vendorName == null) {
			if (other.vendorName != null)
				return false;
		} else if (!vendorName.equals(other.vendorName))
			return false;
		if (vendorRelease == null) {
			if (other.vendorRelease != null)
				return false;
		} else if (!vendorRelease.equals(other.vendorRelease))
			return false;
		return true;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String userId) {
		this.contactId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String resourceName) {
		this.name = resourceName;
	}

	// Icon when using UI import otherwise resourceIconPath
	public String getResourceIconPath() {
		return (resourceIconPath != null) ? resourceIconPath : icon;
	}

	public void setResourceIconPath(String resourceIconPath) {
		this.resourceIconPath = resourceIconPath;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorRelease() {
		return vendorRelease;
	}

	public void setVendorRelease(String vendorRelease) {
		this.vendorRelease = vendorRelease;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public List<CategoryDefinition> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryDefinition> categories) {
		this.categories = categories;
	}

	public void addSubCategory(String category, String subCategory) {
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
