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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.common.api.UploadArtifactInfo;

@NoArgsConstructor
@Data
public class UploadResourceInfo {

    private String payloadData;
    private String payloadName;
    private String description;
    private List<String> tags;
    private List<CategoryDefinition> categories;
    private List<UploadArtifactInfo> artifactList;
    private String contactId;
    private String name;
    private String resourceIconPath;
    private String icon;
    private String vendorName;
    private String vendorRelease;
    private String resourceVendorModelNumber;
    private String resourceType = "VFC";
    private String model;
    private boolean isNormative;
    private String yamlInterfaceName;
    private String yamlInterfaceData;

    public UploadResourceInfo(String payload, String payloadName, String description, String category, List<String> tags,
                              List<UploadArtifactInfo> artifactsList, String modelName) {
        this.payloadData = payload;
        this.payloadName = payloadName;
        this.description = description;
        this.tags = tags;
        this.artifactList = artifactsList;
        this.model = modelName;
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

    // Icon when using UI import otherwise resourceIconPath
    public String getResourceIconPath() {
        return (resourceIconPath != null) ? resourceIconPath : icon;
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
