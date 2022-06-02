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
    private String contactId;
    private String name;
    private String serviceIconPath;
    private String icon;
    private String vendorName;
    private String vendorRelease;
    private String serviceVendorModelNumber;
    private String serviceType = "";
    private String model;
    private Map<String, String> categorySpecificMetadata;
    private String derivedFromGenericType;
    private String derivedFromGenericVersion;

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
