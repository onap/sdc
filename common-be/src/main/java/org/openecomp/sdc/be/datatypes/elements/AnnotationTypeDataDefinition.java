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

package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class AnnotationTypeDataDefinition extends ToscaDataDefinition {

    protected String uniqueId;
    protected String type; 
    protected String description;

    protected Long creationTime;
    protected Long modificationTime;

    protected String version;
    protected boolean highestVersion;

    public AnnotationTypeDataDefinition() {}

    public AnnotationTypeDataDefinition(AnnotationTypeDataDefinition other) {
        uniqueId = other.uniqueId;
        type = other.type;
        version = other.version;
        description = other.description;
        creationTime = other.creationTime;
        modificationTime = other.modificationTime;
        highestVersion = other.highestVersion;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Long modificationTime) {
        this.modificationTime = modificationTime;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isHighestVersion() {
        return highestVersion;
    }

    public void setHighestVersion(boolean highestVersion) {
        this.highestVersion = highestVersion;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": [uniqueId=" + uniqueId + ", type=" + getType()
                + ", version=" + version + ", highestVersion=" + highestVersion
                + ", description=" + description
                + ", creationTime=" + creationTime + ", modificationTime=" + modificationTime + "]";
    }


}
