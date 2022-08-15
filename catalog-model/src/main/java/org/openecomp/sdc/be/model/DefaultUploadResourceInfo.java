/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model;

import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;

public class DefaultUploadResourceInfo extends UploadResourceInfo{

    public DefaultUploadResourceInfo(String toscaName){
        SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName("Network Elements");
        CategoryDefinition category = new CategoryDefinition();
        category.setName("Generic");
        category.setNormalizedName("generic");
        category.setIcons(List.of("defaulticon"));
        category.setNormalizedName("generic");
        category.addSubCategory(subCategory);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        super.setCategories(categories);
        super.setIcon("defaulticon");
        super.setVendorRelease("1");
        super.setNormative(false);
        String[] nodeTemplateName = toscaName.split("\\.");
        String name =  nodeTemplateName[nodeTemplateName.length - 1];
        super.setName(name);
        super.setDescription("A vfc of type " + toscaName);
        List<String> tags = new ArrayList<>();
        tags.add(name);
        super.setTags(tags);
    }
}
