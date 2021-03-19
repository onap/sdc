/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.common.util.ICategorizedElement;

@Getter
@Setter
public class UiLeftPaletteComponent implements ICategorizedElement {

    private final String uniqueId;
    private final String name; // archiveName

    private final String version; // archiveVersion
    private final String description;
    private final List<String> tags;
    private final String icon;
    private final String UUID;
    private final String systemName;
    private final String invariantUUID;
    private final ComponentTypeEnum componentType;
    private final String resourceType;
    private final String categoryName;
    private final String subCategoryName;
    private final String searchFilterTerms;
    private final List<CategoryDefinition> categories;

    public UiLeftPaletteComponent(final Component component) {
        this.uniqueId = component.getUniqueId();
        this.name = component.getName();
        this.version = component.getVersion();
        this.description = component.getDescription();
        this.tags = component.getTags();
        this.icon = component.getIcon();
        this.UUID = component.getUUID();
        this.systemName = component.getSystemName();
        this.invariantUUID = component.getInvariantUUID();
        this.componentType = component.getComponentType();
        this.resourceType = component.getActualComponentType();
        this.categories = component.getCategories();
        this.categoryName = getCategoryName();
        this.subCategoryName = getSubcategoryName();
        this.searchFilterTerms = (name + " " + description + " " + convertListResultToString(tags) + version);
    }

    private String convertListResultToString(final List<String> tags) {
        final StringBuilder sb = new StringBuilder();
        if (tags != null) {
            tags.forEach(t -> sb.append(t + " "));
        }
        return sb.toString().toLowerCase();
    }

    @JsonIgnore
    @Override
    public String getComponentTypeAsString() {
        return componentType.name();
    }

    @JsonIgnore
    public String getCategoryName() {
        return categories.get(0).getName();
    }

    @JsonIgnore
    public String getSubcategoryName() {
        if (componentType == ComponentTypeEnum.SERVICE) {
            return null;
        }
        return categories.get(0).getSubcategories().get(0).getName();
    }
}
