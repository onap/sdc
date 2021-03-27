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
package org.openecomp.sdc.asdctool.impl.validator.utils;

import java.util.List;
import java.util.Map;

public class VfModuleArtifactPayloadEx {

    private String vfModuleModelName, vfModuleModelInvariantUUID, vfModuleModelVersion, vfModuleModelUUID, vfModuleModelCustomizationUUID, vfModuleModelDescription;
    private Boolean isBase;
    private List<String> artifacts;
    private Map<String, Object> properties;

    public String getVfModuleModelName() {
        return vfModuleModelName;
    }

    public void setVfModuleModelName(String vfModuleModelName) {
        this.vfModuleModelName = vfModuleModelName;
    }

    public String getVfModuleModelInvariantUUID() {
        return vfModuleModelInvariantUUID;
    }

    public void setVfModuleModelInvariantUUID(String vfModuleModelInvariantUUID) {
        this.vfModuleModelInvariantUUID = vfModuleModelInvariantUUID;
    }

    public String getVfModuleModelVersion() {
        return vfModuleModelVersion;
    }

    public void setVfModuleModelVersion(String vfModuleModelVersion) {
        this.vfModuleModelVersion = vfModuleModelVersion;
    }

    public String getVfModuleModelUUID() {
        return vfModuleModelUUID;
    }

    public void setVfModuleModelUUID(String vfModuleModelUUID) {
        this.vfModuleModelUUID = vfModuleModelUUID;
    }

    public String getVfModuleModelCustomizationUUID() {
        return vfModuleModelCustomizationUUID;
    }

    public void setVfModuleModelCustomizationUUID(String vfModuleModelCustomizationUUID) {
        this.vfModuleModelCustomizationUUID = vfModuleModelCustomizationUUID;
    }

    public String getVfModuleModelDescription() {
        return vfModuleModelDescription;
    }

    public void setVfModuleModelDescription(String vfModuleModelDescription) {
        this.vfModuleModelDescription = vfModuleModelDescription;
    }

    public Boolean getIsBase() {
        return isBase;
    }

    public void setIsBase(Boolean isBase) {
        this.isBase = isBase;
    }

    public List<String> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<String> artifacts) {
        this.artifacts = artifacts;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
