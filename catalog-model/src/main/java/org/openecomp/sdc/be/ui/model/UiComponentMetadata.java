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
package org.openecomp.sdc.be.ui.model;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

@Getter
@Setter
public abstract class UiComponentMetadata {

    private String uniqueId;
    private String name; // archiveName
    private String tenant;
    private String version; // archiveVersion
    private Boolean isHighestVersion;
    private Long creationDate;
    private Long lastUpdateDate;
    private String description;
    private String lifecycleState;
    private List<String> tags;
    private String icon;
    private String UUID;
    private String normalizedName;
    private String systemName;
    private String contactId;
    private Map<String, String> allVersions;
    private Boolean isDeleted;
    private String projectCode;
    private String csarUUID;
    private String csarVersion;
    private String importedToscaChecksum;
    private String invariantUUID;
    private ComponentTypeEnum componentType;
    private List<CategoryDefinition> categories;
    private String creatorUserId;
    private String creatorFullName;
    private String lastUpdaterUserId;
    private String lastUpdaterFullName;
    //Archive/Restore
    private Boolean isArchived;
    private Long archiveTime;
    private Boolean isVspArchived;
    private String model;



    public UiComponentMetadata() {
    }

    public UiComponentMetadata(List<CategoryDefinition> categories, ComponentMetadataDataDefinition metadata) {
        this.uniqueId = metadata.getUniqueId();
        this.name = metadata.getName(); // archiveName
        this.tenant = metadata.getTenant();
        this.version = metadata.getVersion();
        this.isHighestVersion = metadata.isHighestVersion();
        this.creationDate = metadata.getCreationDate();
        this.lastUpdateDate = metadata.getLastUpdateDate();
        this.description = metadata.getDescription();
        this.lifecycleState = metadata.getState();
        this.tags = metadata.getTags();
        this.icon = metadata.getIcon();
        this.UUID = metadata.getUUID();
        this.normalizedName = metadata.getNormalizedName();
        this.systemName = metadata.getSystemName();
        this.contactId = metadata.getContactId();
        this.allVersions = metadata.getAllVersions();
        this.projectCode = metadata.getProjectCode();
        this.csarUUID = metadata.getCsarUUID();
        this.csarVersion = metadata.getCsarVersion();
        this.importedToscaChecksum = metadata.getImportedToscaChecksum();
        this.invariantUUID = metadata.getInvariantUUID();
        this.componentType = metadata.getComponentType();
        this.categories = categories;
        this.creatorUserId = metadata.getCreatorUserId();
        this.creatorFullName = metadata.getCreatorFullName();
        this.lastUpdaterFullName = metadata.getLastUpdaterFullName();
        this.lastUpdaterUserId = metadata.getLastUpdaterUserId();
        //archive
        this.isArchived = metadata.isArchived();
        this.archiveTime = metadata.getArchiveTime();
        this.isVspArchived = metadata.isVspArchived();
        this.model = metadata.getModel();

    }
}
