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
package org.openecomp.sdc.be.datatypes.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFieldsExtractor;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.common.log.wrappers.Logger;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
public abstract class ComponentMetadataDataDefinition extends ToscaDataDefinition {

    private static final Logger LOGGER = Logger.getLogger(ComponentMetadataDataDefinition.class.getName());
    private String uniqueId;
    private String name; // archiveName
    private String version; // archiveVersion
    private Boolean highestVersion;
    private Long creationDate;
    private Long lastUpdateDate;
    private String description;
    private String state;
    private List<String> tags;
    private String conformanceLevel;
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
    protected ComponentTypeEnum componentType;
    private String creatorUserId;
    private String creatorFullName;
    private String lastUpdaterUserId;
    private String lastUpdaterFullName;
    private Boolean isArchived = false;
    private Long archiveTime;
    private Boolean isVspArchived = false;
    private Map<String, String> categorySpecificMetadata;
    private String model;
    private boolean deleteRestricted = false;

    protected ComponentMetadataDataDefinition(ComponentMetadataDataDefinition other) {
        this.uniqueId = other.getUniqueId();
        this.name = other.getName();
        this.version = other.getVersion();
        this.highestVersion = other.isHighestVersion();
        this.creationDate = other.getCreationDate();
        this.lastUpdateDate = other.getLastUpdateDate();
        this.description = other.getDescription();
        this.state = other.getState();
        this.tags = new ArrayList<>(other.getTags() != null ? other.getTags() : new LinkedList<>());
        this.icon = other.getIcon();
        this.contactId = other.getContactId();
        this.UUID = other.getUUID();
        this.normalizedName = other.getNormalizedName();
        this.systemName = other.getSystemName();
        this.allVersions = new HashMap<>(other.getAllVersions() != null ? other.getAllVersions() : new HashMap<>());
        this.isDeleted = other.getIsDeleted();
        this.projectCode = other.getProjectCode();
        this.csarUUID = other.getCsarUUID();
        this.csarVersion = other.csarVersion;
        this.importedToscaChecksum = other.getImportedToscaChecksum();
        this.invariantUUID = other.getInvariantUUID();
        this.isArchived = other.isArchived;
        this.isVspArchived = other.isVspArchived;
        this.archiveTime = other.getArchiveTime();
        this.categorySpecificMetadata = other.getCategorySpecificMetadata();
        this.model = other.getModel();
        this.deleteRestricted = other.isDeleteRestricted();
    }

    protected ComponentMetadataDataDefinition(JsonPresentationFieldsExtractor extractor) {
        this.uniqueId = extractor.getUniqueId();
        this.name = extractor.getName();
        this.version = extractor.getVersion();
        this.highestVersion = extractor.isHighestVersion();
        this.creationDate = extractor.getCreationDate();
        this.lastUpdateDate = extractor.getLastUpdateDate();
        this.description = extractor.getDescription();
        this.state = extractor.getState();
        this.tags = extractor.getTags();
        this.icon = extractor.getIcon();
        this.contactId = extractor.getContactId();
        this.UUID = extractor.getUUID();
        this.normalizedName = extractor.getNormalizedName();
        this.systemName = extractor.getSystemName();
        this.isDeleted = extractor.isDeleted();
        this.projectCode = extractor.getProjectCode();
        this.csarUUID = extractor.getCsarUuid();
        this.csarVersion = extractor.getCsarVersion();
        this.importedToscaChecksum = extractor.getImportedToscaChecksum();
        this.invariantUUID = extractor.getInvariantUuid();
        this.isArchived = extractor.isArchived();
        this.isVspArchived = extractor.isVspArchived();
        this.archiveTime = extractor.getArchiveTime();
        this.model = extractor.getModel();
        this.deleteRestricted = extractor.isDeleteRestricted() == null ? false: extractor.isDeleteRestricted();
    }

    public void setUniqueId(String uniqueId) {
        if (this.uniqueId != null && !this.uniqueId.equals(uniqueId)) {
            LOGGER.warn("uniqueId changed more then once -> OLD : {} , NEW: {} ", this.uniqueId, uniqueId);
        }
        this.uniqueId = uniqueId;
    }

    public void setUUID(String UUID) {
        if (this.UUID != null && !this.UUID.equals(UUID)) {
            LOGGER.warn("UUID changed more then once -> OLD : {} , NEW: {} ", this.UUID, UUID);
        }
        this.UUID = UUID;
    }

    public void setInvariantUUID(String invariantUUID) {
        if (this.invariantUUID != null && !this.invariantUUID.equals(invariantUUID)) {
            LOGGER.warn("InvariantUUID changed more then once -> OLD : {} , NEW: {} ", this.invariantUUID, invariantUUID);
        }
        this.invariantUUID = invariantUUID;
    }

    public Boolean isHighestVersion() {
        return highestVersion;
    }

    public String getLifecycleState() {
        return state;
    }

    public void setLifecycleState(String state) {
        this.state = state;
    }

    public Boolean isDeleted() {
        return getIsDeleted();
    }

    public Boolean isArchived() {
        return getIsArchived();
    }

    public void setArchived(Boolean archived) {
        setIsArchived(archived);
    }

    public Boolean isVspArchived() {
        return getIsVspArchived();
    }

    public void setVspArchived(Boolean vspArchived) {
        setIsVspArchived(vspArchived);
    }

    /**
     * Return the type of the actual component - e.g. for a Resource, return the actual VF/CR
     *
     * @return
     */
    public abstract String getActualComponentType();
}
