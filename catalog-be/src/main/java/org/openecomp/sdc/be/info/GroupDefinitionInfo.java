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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.info;

import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class GroupDefinitionInfo {
    private String name;

    private String description;

    // the id is unique per group instance on graph.
    private String uniqueId;

    // the id is unique per group instance on graph.
    private String groupInstanceUniqueId;

    // the group UUID should be changed when one of the artifacts/component
    // instances has been changed.
    private String groupUUID;

    // version should be changed when there is a change to the group's metadata
    // or to the groups members
    // (not necessarily when the VF version is changed). This field cannot be
    // updated by user
    private String version;

    private String invariantUUID;
    private String customizationUUID;

    private Boolean isBase = null;

    // artifacts - list of artifact uid. All artifacts in the group must already
    // be uploaded to the VF
    private List<ArtifactDefinitionInfo> artifacts;

    private Map<String, String> members;

    private List<? extends GroupProperty> properties;

    public GroupDefinitionInfo() {}

    public GroupDefinitionInfo(GroupDefinition other) {
        this.setName(other.getName());
        this.setDescription(other.getDescription());
        this.setUniqueId(other.getUniqueId());
        this.setVersion(other.getVersion());
        this.setGroupUUID(other.getGroupUUID());
        this.setInvariantUUID(other.getInvariantUUID());
        this.setProperties(other.convertToGroupProperties());
        if (other.getMembers() != null) {
            this.members = new HashMap<>(other.getMembers());
        }
    }

    public GroupDefinitionInfo(GroupInstance other) {
        this.setName(other.getGroupName());
        this.setDescription(other.getDescription());
        this.setUniqueId(other.getGroupUid());
        this.setGroupInstanceUniqueId(other.getUniqueId());
        this.setVersion(other.getVersion());
        this.setGroupUUID(other.getGroupUUID());
        this.setCustomizationUUID(other.getCustomizationUUID());
        this.setInvariantUUID(other.getInvariantUUID());
        this.setProperties(other.convertToGroupInstancesProperties());
    }

    public String getInvariantUUID() {
        return invariantUUID;
    }

    public void setInvariantUUID(String invariantUUID) {
        this.invariantUUID = invariantUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getGroupUUID() {
        return groupUUID;
    }

    public void setGroupUUID(String groupUUID) {
        this.groupUUID = groupUUID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCustomizationUUID() {
        return customizationUUID;
    }

    public void setCustomizationUUID(String customizationUUID) {
        this.customizationUUID = customizationUUID;
    }

    public Boolean getIsBase() {
        return isBase;
    }

    public void setIsBase(Boolean isBase) {
        this.isBase = isBase;
    }

    public List<ArtifactDefinitionInfo> getArtifacts() {
        return (artifacts==null) ? null : new ArrayList<>(artifacts);
    }

    public void setArtifacts(List<ArtifactDefinitionInfo> artifacts) {
        this.artifacts = (artifacts==null) ? null : new ArrayList<>(artifacts);
    }

    public List<GroupProperty> getProperties() {
        return (properties==null) ? null : new ArrayList<>(properties);
    }

    public void setProperties(List<? extends GroupProperty> properties) {
        this.properties = (properties==null) ? null : new ArrayList<>(properties);
    }

    public String getGroupInstanceUniqueId() {
        return groupInstanceUniqueId;
    }

    public void setGroupInstanceUniqueId(String groupInstanceUniqueId) {
        this.groupInstanceUniqueId = groupInstanceUniqueId;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "GroupDefinitionInfo [" + super.toString() + ", isBase=" + isBase + ", artifacts=" + artifacts + "]";
    }
}
