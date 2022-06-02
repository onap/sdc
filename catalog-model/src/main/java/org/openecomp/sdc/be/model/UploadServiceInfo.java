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
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.common.api.UploadArtifactInfo;

@Getter
@Setter
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
    private String serviceFunction;
    private String environmentContext;
    private String instantiationType;
    private String projectCode;
    private List<UploadArtifactInfo> artifactList;
    private String contactId, name, serviceIconPath, icon, vendorName, vendorRelease, serviceVendorModelNumber;
    private String serviceType = "";
    private String model;
    private Map<String, String> categorySpecificMetadata;
    private String derivedFromGenericType;
    private String derivedFromGenericVersion;

    public UploadServiceInfo(String payloadData, String payloadName, String description, List<String> tags, String invariantUUID, String UUID,
                             String type, String category, String subcategory, String resourceVendor, String resourceVendorRelease,
                             String serviceRole, String serviceEcompNaming, String ecompGeneratedNaming, String namingPolicy, String serviceFunction,
                             String environmentContext, String instantiationType, List<UploadArtifactInfo> artifactList, String contactId, String name,
                             String resourceIconPath, String icon, String vendorName, String vendorRelease, String serviceVendorModelNumber,
                             String serviceType, String projectCode, String model, Map<String, String> categorySpecificMetadata,
                             String derivedFromGenericType, String derivedFromGenericVersion) {
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
        this.serviceFunction = serviceFunction;
        this.environmentContext = environmentContext;
        this.instantiationType = instantiationType;
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
        this.model = model;
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
        this.categorySpecificMetadata = categorySpecificMetadata;
        this.derivedFromGenericType = derivedFromGenericType;
        this.derivedFromGenericVersion = derivedFromGenericVersion;
    }

    public UploadServiceInfo() {
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
