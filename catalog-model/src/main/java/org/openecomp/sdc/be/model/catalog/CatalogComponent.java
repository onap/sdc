/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.model.catalog;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

@Getter
@Setter
public class CatalogComponent {

    private String version;
    private ComponentTypeEnum componentType;
    private String icon;
    private String uniqueId;
    private String lifecycleState;
    private long lastUpdateDate;
    private String name;
    private String resourceType;
    private String categoryNormalizedName;
    private String subCategoryNormalizedName;
    private String distributionStatus;
    private String uuid;
    private String invariantUUID;
    private String systemName;
    private String description;
    private List<String> tags;
    private Boolean isHighestVersion;
    private String lastUpdaterUserId;
    private List<CategoryDefinition> categories;
    private String model;
    private String tenant;

    public List<String> getTags() {
        return tags == null ? Collections.emptyList() : ImmutableList.copyOf(tags);
    }

    public void setTags(List<String> tags) {
        requireNonNull(tags);
        this.tags = new ArrayList<>(tags);
    }

    public List<CategoryDefinition> getCategories() {
        return categories == null ? Collections.emptyList() : ImmutableList.copyOf(categories);
    }
}
