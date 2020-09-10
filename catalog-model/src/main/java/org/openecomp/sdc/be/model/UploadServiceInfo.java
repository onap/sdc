/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 CMCC Intellectual Property. All rights reserved.
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


//upload Service model by Shiyong1989@hotmail.com
public class UploadServiceInfo {

  private String payloadData;
  private String payloadName;
  private String description;
  private List<String> tags;
  private List<CategoryDefinition> categories;

  private String invariantUUID;
  private String UUID;
  private String type;
  private String category;
  private String subcategory;
  private String resourceVendor;
  private String resourceVendorRelease;
  private String serviceRole;
  private String serviceEcompNaming;
  private String ecompGeneratedNaming;
  private String namingPolicy;
  private String projectCode;


  private List<UploadArtifactInfo> artifactList;
  private String contactId, name, serviceIconPath, icon, vendorName, vendorRelease, serviceVendorModelNumber;

  private String serviceType = "";

  public UploadServiceInfo(String payloadData, String payloadName, String description,
      List<String> tags, String invariantUUID, String UUID, String type,
      String category, String subcategory, String resourceVendor,
      String resourceVendorRelease, String serviceRole, String serviceEcompNaming,
      String ecompGeneratedNaming, String namingPolicy,
      List<UploadArtifactInfo> artifactList, String contactId, String name,
      String resourceIconPath, String icon, String vendorName, String vendorRelease,
      String serviceVendorModelNumber, String serviceType, String projectCode) {
    this.payloadData = payloadData;
    this.payloadName = payloadName;
    this.description = description;
    this.tags = tags;
    this.invariantUUID = invariantUUID;
    this.UUID = UUID;
    this.type = type;
    this.category = category;
    this.subcategory = subcategory;
    this.resourceVendor = resourceVendor;
    this.resourceVendorRelease = resourceVendorRelease;
    this.serviceRole = serviceRole;
    this.serviceEcompNaming = serviceEcompNaming;
    this.ecompGeneratedNaming = ecompGeneratedNaming;
    this.namingPolicy = namingPolicy;
    this.artifactList = artifactList;
    this.contactId = contactId;
    this.name = name;
    this.serviceIconPath = serviceIconPath;
    this.icon = icon;
    this.vendorName = vendorName;
    this.vendorRelease = vendorRelease;
    this.serviceVendorModelNumber = serviceVendorModelNumber;
    this.serviceType = serviceType;
    this.projectCode = projectCode;

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

  public UploadServiceInfo() {
  }


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

  public String getInvariantUUID() {
    return invariantUUID;
  }

  public void setInvariantUUID(String invariantUUID) {
    this.invariantUUID = invariantUUID;
  }

  public String getUUID() {
    return UUID;
  }

  public void setUUID(String UUID) {
    this.UUID = UUID;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSubcategory() {
    return subcategory;
  }

  public void setSubcategory(String subcategory) {
    this.subcategory = subcategory;
  }

  public String getResourceVendor() {
    return resourceVendor;
  }

  public void setResourceVendor(String resourceVendor) {
    this.resourceVendor = resourceVendor;
  }

  public String getResourceVendorRelease() {
    return resourceVendorRelease;
  }

  public void setResourceVendorRelease(String resourceVendorRelease) {
    this.resourceVendorRelease = resourceVendorRelease;
  }

  public String getServiceRole() {
    return serviceRole;
  }

  public void setServiceRole(String serviceRole) {
    this.serviceRole = serviceRole;
  }

  public String getServiceEcompNaming() {
    return serviceEcompNaming;
  }

  public void setServiceEcompNaming(String serviceEcompNaming) {
    this.serviceEcompNaming = serviceEcompNaming;
  }

  public String getEcompGeneratedNaming() {
    return ecompGeneratedNaming;
  }

  public void setEcompGeneratedNaming(String ecompGeneratedNaming) {
    this.ecompGeneratedNaming = ecompGeneratedNaming;
  }

  public String getNamingPolicy() {
    return namingPolicy;
  }

  public void setNamingPolicy(String namingPolicy) {
    this.namingPolicy = namingPolicy;
  }

  public String getIcon() {
    return icon;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((artifactList == null) ? 0 : artifactList.hashCode());
    result = prime * result + ((contactId == null) ? 0 : contactId.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((icon == null) ? 0 : icon.hashCode());
    result = prime * result + ((payloadData == null) ? 0 : payloadData.hashCode());
    result = prime * result + ((payloadName == null) ? 0 : payloadName.hashCode());
    result = prime * result + ((serviceIconPath == null) ? 0 : serviceIconPath.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((tags == null) ? 0 : tags.hashCode());
    result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
    result = prime * result + ((vendorRelease == null) ? 0 : vendorRelease.hashCode());
    result = prime * result + ((serviceVendorModelNumber == null) ? 0 : serviceVendorModelNumber.hashCode());
    result = prime * result + ((invariantUUID == null) ? 0 : invariantUUID.hashCode());
    result = prime * result + ((UUID == null) ? 0 : UUID.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((category == null) ? 0 : category.hashCode());
    result = prime * result + ((resourceVendor == null) ? 0 : resourceVendor.hashCode());
    result = prime * result + ((resourceVendorRelease == null) ? 0 : resourceVendorRelease.hashCode());
    result = prime * result + ((serviceRole == null) ? 0 : serviceRole.hashCode());
    result = prime * result + ((serviceEcompNaming == null) ? 0 : serviceEcompNaming.hashCode());
    result = prime * result + ((ecompGeneratedNaming == null) ? 0 : ecompGeneratedNaming.hashCode());
    result = prime * result + ((namingPolicy == null) ? 0 : namingPolicy.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    UploadServiceInfo other = (UploadServiceInfo) obj;
    if (artifactList == null) {
      if (other.artifactList != null) {
        return false;
      }
    } else if (!artifactList.equals(other.artifactList)) {
      return false;
    }
    if (contactId == null) {
      if (other.contactId != null) {
        return false;
      }
    } else if (!contactId.equals(other.contactId)) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (icon == null) {
      if (other.icon != null) {
        return false;
      }
    } else if (!icon.equals(other.icon)) {
      return false;
    }
    if (payloadData == null) {
      if (other.payloadData != null) {
        return false;
      }
    } else if (!payloadData.equals(other.payloadData)) {
      return false;
    }
    if (payloadName == null) {
      if (other.payloadName != null) {
        return false;
      }
    } else if (!payloadName.equals(other.payloadName)) {
      return false;
    }
    if (serviceIconPath == null) {
      if (other.serviceIconPath != null) {
        return false;
      }
    } else if (!serviceIconPath.equals(other.serviceIconPath)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (tags == null) {
      if (other.tags != null) {
        return false;
      }
    } else if (!tags.equals(other.tags)) {
      return false;
    }
    if (vendorName == null) {
      if (other.vendorName != null) {
        return false;
      }
    } else if (!vendorName.equals(other.vendorName)) {
      return false;
    }
    if (serviceVendorModelNumber == null) {
      if (other.serviceVendorModelNumber != null) {
        return false;
      }
    } else if (!serviceVendorModelNumber.equals(other.serviceVendorModelNumber)) {
      return false;
    }
    if (vendorRelease == null) {
      if (other.vendorRelease != null) {
        return false;
      }
    } else if (!vendorRelease.equals(other.vendorRelease)) {
      return false;
    }
    if (invariantUUID == null) {
      if (other.invariantUUID != null) {
        return false;
      }
    } else if (!invariantUUID.equals(other.invariantUUID)) {
      return false;
    }
    if (UUID == null) {
      if (other.UUID != null) {
        return false;
      }
    } else if (!UUID.equals(other.UUID)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    if (subcategory == null) {
      if (other.subcategory != null) {
        return false;
      }
    } else if (!subcategory.equals(other.subcategory)) {
      return false;
    }
    if (resourceVendor == null) {
      if (other.resourceVendor != null) {
        return false;
      }
    } else if (!resourceVendor.equals(other.resourceVendor)) {
      return false;
    }
    if (resourceVendorRelease == null) {
      if (other.resourceVendorRelease != null) {
        return false;
      }
    } else if (!resourceVendorRelease.equals(other.resourceVendorRelease)) {
      return false;
    }
    if (serviceRole == null) {
      if (other.serviceRole != null) {
        return false;
      }
    } else if (!serviceRole.equals(other.serviceRole)) {
      return false;
    }
    if (serviceEcompNaming == null) {
      if (other.serviceEcompNaming != null) {
        return false;
      }
    } else if (!serviceEcompNaming.equals(other.serviceEcompNaming)) {
      return false;
    }
    if (ecompGeneratedNaming == null) {
      if (other.ecompGeneratedNaming != null) {
        return false;
      }
    } else if (!ecompGeneratedNaming.equals(other.ecompGeneratedNaming)) {
      return false;
    }
    if (namingPolicy == null) {
      if (other.namingPolicy != null) {
        return false;
      }
    } else if (!namingPolicy.equals(other.namingPolicy)) {
      return false;
    }
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

  // Icon when using UI import otherwise serviceIconPath
  public String getServiceIconPath() {
    return (serviceIconPath != null) ? serviceIconPath : icon;
  }

  public void setServiceIconPath(String serviceIconPath) {
    this.serviceIconPath = serviceIconPath;
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

  public String getServiceVendorModelNumber() {
    return serviceVendorModelNumber;
  }

  public void setServiceVendorModelNumber(String serviceVendorModelNumber) {
    this.serviceVendorModelNumber = serviceVendorModelNumber;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
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

  public String getProjectCode() {
    return projectCode;
  }

  public void setProjectCode(String projectCode) {
    this.projectCode = projectCode;
  }
}