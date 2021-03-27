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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

/**
 * Represents the requirement of the component or component instance
 */
@EqualsAndHashCode
@ToString
public class RequirementDataDefinition extends ToscaDataDefinition {

    public static final String MIN_OCCURRENCES = "0";
    public static final String MAX_OCCURRENCES = "UNBOUNDED";
    public static final String MAX_DEFAULT_OCCURRENCES = "1";
    @Getter
    @Setter
    public boolean external = false;

    /**
     * The default constructor initializing limits of the occurrences
     */
    public RequirementDataDefinition() {
        this.setMinOccurrences(MIN_OCCURRENCES);
        this.setMaxOccurrences(MAX_OCCURRENCES);
        this.setLeftOccurrences(MAX_OCCURRENCES);
    }

    /**
     * Deep copy constructor
     *
     * @param other
     */
    public RequirementDataDefinition(RequirementDataDefinition other) {
        this.setUniqueId(other.getUniqueId());
        this.setName(other.getName());
        this.setParentName(other.getParentName());
        this.setPreviousName(other.getPreviousName());
        this.setCapability(other.getCapability());
        this.setNode(other.getNode());
        this.setRelationship(other.getRelationship());
        this.setOwnerId(other.getOwnerId());
        this.setOwnerName(other.getOwnerName());
        this.setMinOccurrences(other.getMinOccurrences());
        this.setMaxOccurrences(other.getMaxOccurrences());
        this.setLeftOccurrences(other.getLeftOccurrences());
        if (other.getPath() == null) {
            this.setPath(Lists.newArrayList());
        } else {
            this.setPath(Lists.newArrayList(other.getPath()));
        }
        this.setSource(other.getSource());
        this.setExternal(other.isExternal());
    }

    /**
     * Unique id of the requirement
     */
    public String getUniqueId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
    }

    public void setUniqueId(String uniqueId) {
        setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
    }

    public String getParentName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.PARENT_NAME);
    }

    public void setParentName(String parentName) {
        setToscaPresentationValue(JsonPresentationFields.PARENT_NAME, parentName);
    }

    public String getPreviousName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.PREVIOUS_NAME);
    }

    public void setPreviousName(String previousName) {
        setToscaPresentationValue(JsonPresentationFields.PREVIOUS_NAME, previousName);
    }

    /**
     * specify the capability type
     */
    public String getCapability() {
        return (String) getToscaPresentationValue(JsonPresentationFields.CAPABILITY);
    }

    public void setCapability(String capability) {
        setToscaPresentationValue(JsonPresentationFields.CAPABILITY, capability);
    }

    /**
     * specify the node type(Optional by tosca)
     */
    public String getNode() {
        return (String) getToscaPresentationValue(JsonPresentationFields.NODE);
    }

    public void setNode(String node) {
        setToscaPresentationValue(JsonPresentationFields.NODE, node);
    }

    /**
     * specify the relationship type(Optional by tosca)
     */
    public String getRelationship() {
        return (String) getToscaPresentationValue(JsonPresentationFields.RELATIONSHIP);
    }

    public void setRelationship(String relationship) {
        setToscaPresentationValue(JsonPresentationFields.RELATIONSHIP, relationship);
    }

    /**
     * specifies the resource instance holding this requirement
     */
    @Override
    public String getOwnerId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_ID);
    }

    @Override
    public void setOwnerId(String ownerId) {
        setToscaPresentationValue(JsonPresentationFields.OWNER_ID, ownerId);
    }

    public String getOwnerName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_NAME);
    }

    public void setOwnerName(String ownerName) {
        setToscaPresentationValue(JsonPresentationFields.OWNER_NAME, ownerName);
    }

    public String getMinOccurrences() {
        return (String) getToscaPresentationValue(JsonPresentationFields.MIN_OCCURRENCES);
    }

    public void setMinOccurrences(String minOccurrences) {
        setToscaPresentationValue(JsonPresentationFields.MIN_OCCURRENCES, minOccurrences);
    }

    public String getLeftOccurrences() {
        return (String) getToscaPresentationValue(JsonPresentationFields.LEFT_OCCURRENCES);
    }

    public void setLeftOccurrences(String leftOccurrences) {
        setToscaPresentationValue(JsonPresentationFields.LEFT_OCCURRENCES, leftOccurrences);
    }

    public String getMaxOccurrences() {
        return (String) getToscaPresentationValue(JsonPresentationFields.MAX_OCCURRENCES);
    }

    public void setMaxOccurrences(String maxOccurrences) {
        setToscaPresentationValue(JsonPresentationFields.MAX_OCCURRENCES, maxOccurrences);
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getPath() {
        return (List<String>) getToscaPresentationValue(JsonPresentationFields.PATH);
    }

    public void setPath(List<String> path) {
        setToscaPresentationValue(JsonPresentationFields.PATH, path);
    }

    public String getSource() {
        return (String) getToscaPresentationValue(JsonPresentationFields.SOURCE);
    }

    public void setSource(String source) {
        setToscaPresentationValue(JsonPresentationFields.SOURCE, source);
    }

    /**
     * Adds the element to the path avoiding duplication
     *
     * @param elementInPath
     */
    public void addToPath(String elementInPath) {
        List<String> path = getPath();
        if (path == null) {
            path = new ArrayList<>();
        }
        if (!path.contains(elementInPath)) {
            path.add(elementInPath);
        }
        setPath(path);
    }
}
