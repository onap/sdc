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
package org.openecomp.sdc.be.catalog.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.openecomp.sdc.be.catalog.api.IComponentMessage;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;

public class ComponentMessage extends CatalogComponent implements IComponentMessage {

    /**
     *
     */
    private static final long serialVersionUID = 3233307722573636520L;
    @JsonProperty("changeTypeEnum")
    ChangeTypeEnum changeTypeEnum;
    @JsonProperty("catalogUpdateTimestamp")
    private CatalogUpdateTimestamp catalogUpdateTimestamp;
    private Boolean isArchived;

    public ComponentMessage(Component component, ChangeTypeEnum changeTypeEnum, CatalogUpdateTimestamp catalogUpdateTimestamp) {
        super();
        this.changeTypeEnum = changeTypeEnum;
        this.catalogUpdateTimestamp = catalogUpdateTimestamp;
        setUniqueId(component.getUniqueId());// uniqueId

        setUuid(component.getUUID()); // uuid

        setInvariantUUID(component.getInvariantUUID()); // invariantUUID

        // View Fields

        setName(component.getName()); // name

        setSystemName(component.getSystemName()); // systemName

        setVersion(component.getVersion());// version
        setLifecycleState(component.getLifecycleState()
            .name()); // lifecycleState

        setIcon(component.getIcon()); // icon
        ComponentTypeEnum componentType = component.getComponentType();
        setComponentType(componentType);// componentType

        buildCategories(component.getCategories()); // categoryNormalizedName,

        // subCategoryNormalizedName
        if (componentType == ComponentTypeEnum.SERVICE) {
            Service service = (Service) component;
            setDistributionStatus(service.getDistributionStatus()
                .name()); // distributionStatus
        } else {
            Resource r = (Resource) component;
            this.setResourceType(r.getResourceType()
                .name()); // resourceType
        }
        setIsArchived(component.isArchived()); // isArchived

        setIsHighestVersion(component.isHighestVersion()); // isHighestVersion

        setDescription(component.getDescription()); // description

        setTenant(component.getTenant()); // tenant

        if (component.getTags() != null) {
            setTags(component.getTags()); // tags
        }
        setLastUpdateDate(component.getLastUpdateDate());// lastUpdateDate
        setLastUpdaterUserId(component.getLastUpdaterUserId());
    }

    private void buildCategories(List<CategoryDefinition> categories) {
        if (categories != null) {
            setCategories(categories);
            CategoryDefinition categoryDefinition = categories.get(0);
            if (categoryDefinition != null) {
                setCategoryNormalizedName(categoryDefinition.getName());
                List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
                if (null != subcategories) {
                    SubCategoryDefinition subCategoryDefinition = subcategories.get(0);
                    if (subCategoryDefinition != null) {
                        setSubCategoryNormalizedName(subCategoryDefinition.getName());
                    }
                }
            }
        }
    }

    @Override
    public ChangeTypeEnum getChangeType() {
        return changeTypeEnum;
    }

    @Override
    public CatalogUpdateTimestamp getCatalogUpdateTimestamp() {
        return catalogUpdateTimestamp;
    }

    @Override
    public String toString() {
        return "ComponentMessage [ getChangeType()=" + getChangeType() + ", getCatalogUpdateTimestamp()=" + getCatalogUpdateTimestamp()
            + ", getIsArchived()=" + getIsArchived() + ", getUuid()=" + getUuid() + ", getInvariantUUID()=" + getInvariantUUID()
            + ", getSystemName()=" + getSystemName() + ", getDescription()=" + getDescription() + ", getIsHighestVersion()=" + getIsHighestVersion()
            + ", getCategoryNormalizedName()=" + getCategoryNormalizedName() + ", getSubCategoryNormalizedName()=" + getSubCategoryNormalizedName()
            + ", getResourceType()=" + getResourceType() + ", getName()=" + getName() + ", getLastUpdateDate()=" + getLastUpdateDate()
            + ", getVersion()=" + getVersion() + ", getComponentType()=" + getComponentType() + ", getIcon()=" + getIcon() + ", getUniqueId()="
            + getUniqueId() + ", getLifecycleState()=" + getLifecycleState() + ", getDistributionStatus()=" + getDistributionStatus() + ", getTags()="
            + getTags() + ", getCategories()=" + getCategories() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
            + super.toString() + "]";
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    @Override
    public String getMessageType() {
        return getClass().getSimpleName();
    }
}
