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

package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;


public class GroupDataDefinition extends ToscaDataDefinition {
    @JsonInclude
    private String typeUid;
    @JsonInclude
    private Integer propertyValueCounter = 0;

    public GroupDataDefinition() {
    }

    public GroupDataDefinition(Map<String, Object> gr) {
        super(gr);
        propertyValueCounter = 0;
    }

    public GroupDataDefinition(GroupDataDefinition other) {
        this.setName(other.getName());
        this.setUniqueId(other.getUniqueId());
        this.setType(other.getType());
        this.setVersion(other.getVersion());
        this.setInvariantUUID(other.getInvariantUUID());
        this.setDescription(other.getDescription());
        this.propertyValueCounter = other.propertyValueCounter;
        this.setGroupUUID(other.getGroupUUID());
        this.setInvariantName(other.getInvariantName());
        this.setCreatedFrom(other.getCreatedFrom());

        if (other.getMembers() != null) {
            this.setMembers(new HashMap<>(other.getMembers()));
        }
        if (other.getArtifacts() != null) {
            this.setArtifacts(new ArrayList<>(other.getArtifacts()));
        }

        if (other.getArtifactsUuid() != null) {
            this.setArtifactsUuid(new ArrayList<>(other.getArtifactsUuid()));
        }
        if (other.getProperties() != null) {
            this.setProperties(new ArrayList<>(other.getProperties()));
        }
        this.setTypeUid(other.typeUid);
    }


    public String getName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
    }

    public void setName(String name) {
        setToscaPresentationValue(JsonPresentationFields.NAME, name);
    }

    public String getInvariantName() {
        String invariantName = (String) getToscaPresentationValue(JsonPresentationFields.CI_INVARIANT_NAME);
        return invariantName == null ? getName() : invariantName;
    }

    public void setInvariantName(String invariantName) {
        setToscaPresentationValue(JsonPresentationFields.CI_INVARIANT_NAME, invariantName);
    }

    public CreatedFrom getCreatedFrom() {
        String createdFrom = (String) getToscaPresentationValue(JsonPresentationFields.CREATED_FROM);
        return createdFrom == null ? CreatedFrom.CSAR : CreatedFrom.valueOf(createdFrom);
    }

    public void setCreatedFrom(CreatedFrom createdFrom) {
        setToscaPresentationValue(JsonPresentationFields.CREATED_FROM, createdFrom.name());
    }

    public String getUniqueId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
    }

    public void setUniqueId(String uniqueId) {
        setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
    }

    public String getType() {
        return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
    }

    public void setType(String type) {
        setToscaPresentationValue(JsonPresentationFields.TYPE, type);
    }

    public String getVersion() {
        return (String) getToscaPresentationValue(JsonPresentationFields.VERSION);
    }

    public void setVersion(String version) {
        setToscaPresentationValue(JsonPresentationFields.VERSION, version);
    }

    public String getInvariantUUID() {
        return (String) getToscaPresentationValue(JsonPresentationFields.INVARIANT_UUID);
    }

    public void setInvariantUUID(String invariantUUID) {
        setToscaPresentationValue(JsonPresentationFields.INVARIANT_UUID, invariantUUID);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
    }

    public void setDescription(String description) {
        setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
    }

    public Integer getPropertyValueCounter() {
        return propertyValueCounter;
    }

    public void setPropertyValueCounter(Integer propertyValueCounter) {
        this.propertyValueCounter = propertyValueCounter;
    }

    public String getGroupUUID() {
        return (String) getToscaPresentationValue(JsonPresentationFields.GROUP_UUID);
    }

    public void setGroupUUID(String groupUUID) {
        setToscaPresentationValue(JsonPresentationFields.GROUP_UUID, groupUUID);
    }

    public Map<String, String> getMembers() {
        return (Map<String, String>) getToscaPresentationValue(JsonPresentationFields.GROUP_MEMBER);
    }

    //this is used by GroupCompositionMixin
    public Map<String, String> resolveMembersList() {
        Map<String, String> members = getMembers();
        if (members != null) {
            return members;
        }
        return emptyMap();

    }

    public void setMembers(Map<String, String> members) {
        setToscaPresentationValue(JsonPresentationFields.GROUP_MEMBER, members);
    }

    public List<String> getArtifacts() {
        return (List<String>) getToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS);
    }

    public void setArtifacts(List<String> artifacts) {
        setToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS, artifacts);
    }

    public List<String> getArtifactsUuid() {
        return (List<String>) getToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS_UUID);
    }

    public void setArtifactsUuid(List<String> artifactsUuid) {
        setToscaPresentationValue(JsonPresentationFields.GROUP_ARTIFACTS_UUID, artifactsUuid);
    }

    public List<PropertyDataDefinition> getProperties() {
        return (List<PropertyDataDefinition>) getToscaPresentationValue(JsonPresentationFields.GROUP_PROPERTIES);
    }

    public void setProperties(List<PropertyDataDefinition> properties) {
        setToscaPresentationValue(JsonPresentationFields.GROUP_PROPERTIES, properties);
    }

    public String getTypeUid() {
        return typeUid;
    }

    public void setTypeUid(String typeUid) {
        this.typeUid = typeUid;
    }

    public boolean isUserDefined() {
        return CreatedFrom.UI.equals(getCreatedFrom());
    }

    public boolean isVspOriginated() {
        return CreatedFrom.CSAR.equals(getCreatedFrom());
    }

    @Override
    public String toString() {
        return "GroupDataDefinition [propertyValueCounter=" + propertyValueCounter + ", toscaPresentation=" + toscaPresentation + ", getName()=" + getName() + ", getUniqueId()=" + getUniqueId() + ", getType()=" + getType() + ", getVersion()="
                + getVersion() + ", getInvariantUUID()=" + getInvariantUUID() + ", getDescription()=" + getDescription() + ", getPropertyValueCounter()=" + getPropertyValueCounter() + ", getGroupUUID()=" + getGroupUUID() + ", getMembers()="
                + getMembers() + ", getArtifacts()=" + getArtifacts() + ", getArtifactsUuid()=" + getArtifactsUuid() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
                + super.toString() + "]";
    }


    public boolean containsInstanceAsMember(String instanceId) {
        return getMembers() != null && getMembers().values().contains(instanceId);
    }
}
