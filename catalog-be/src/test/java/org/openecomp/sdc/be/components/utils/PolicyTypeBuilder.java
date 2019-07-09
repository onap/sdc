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

package org.openecomp.sdc.be.components.utils;


import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

import java.util.List;
import java.util.Map;

public class PolicyTypeBuilder {

    private PolicyTypeDefinition policyTypeDataDefinition;

    public PolicyTypeBuilder() {
        this.policyTypeDataDefinition = new PolicyTypeDefinition();
    }

    public PolicyTypeBuilder setType(String type) {
        policyTypeDataDefinition.setType(type);
        return this;
    }

    public PolicyTypeBuilder setUniqueId(String uid) {
        policyTypeDataDefinition.setUniqueId(uid);
        return this;
    }

    public PolicyTypeBuilder setDerivedFrom(String derivedFrom) {
        policyTypeDataDefinition.setDerivedFrom(derivedFrom);
        return this;
    }

    public PolicyTypeBuilder setVersion(String version) {
        policyTypeDataDefinition.setVersion(version);
        return this;
    }

    public PolicyTypeBuilder setDescription(String version) {
        policyTypeDataDefinition.setDescription(version);
        return this;
    }

    public PolicyTypeBuilder setTargets(List<String> targets) {
        policyTypeDataDefinition.setTargets(targets);
        return this;
    }

    public PolicyTypeBuilder setMetadata(Map<String, String> metadata) {
        policyTypeDataDefinition.setMetadata(metadata);
        return this;
    }

    public PolicyTypeBuilder setModificationTime(long modificationTime) {
        policyTypeDataDefinition.setModificationTime(modificationTime);
        return this;
    }

    public PolicyTypeBuilder setCreationTime(long creationTime) {
        policyTypeDataDefinition.setModificationTime(creationTime);
        return this;
    }

    public PolicyTypeBuilder setOwner(String owner) {
        policyTypeDataDefinition.setOwnerId(owner);
        return this;
    }

    public PolicyTypeBuilder setName(String name) {
        policyTypeDataDefinition.setName(name);
        return this;
    }

    public PolicyTypeBuilder setIcon(String icon) {
        policyTypeDataDefinition.setIcon(icon);
        return this;
    }

    public PolicyTypeBuilder setHighestVersion(boolean isHighestVersion) {
        policyTypeDataDefinition.setHighestVersion(isHighestVersion);
        return this;
    }

    public PolicyTypeBuilder setProperties(List<PropertyDefinition> properties) {
        policyTypeDataDefinition.setProperties(properties);
        return this;
    }

    public PolicyTypeDefinition build() {
        return policyTypeDataDefinition;
    }



}
