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

package org.openecomp.sdc.be.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UploadComponentInstanceInfo {
    private String name;
    private String type;
    private Map<String, List<UploadCapInfo>> capabilities;
    private Map<String, List<UploadReqInfo>> requirements;
    private Map<String, List<UploadPropInfo>> properties;
    private Map<String, String> capabilitiesNamesToUpdate;
    private Map<String, String> requirementsNamesToUpdate;
    private Collection<String> directives;
    private UploadNodeFilterInfo uploadNodeFilterInfo;

    public Map<String, List<UploadPropInfo>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, List<UploadPropInfo>> properties) {
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, List<UploadCapInfo>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, List<UploadCapInfo>> capabilities) {
        this.capabilities = capabilities;
    }

    public Map<String, List<UploadReqInfo>> getRequirements() {
        return requirements;
    }

    public void setRequirements(Map<String, List<UploadReqInfo>> requirements) {
        this.requirements = requirements;
    }

    public Map<String, String> getCapabilitiesNamesToUpdate() {
        return capabilitiesNamesToUpdate;
    }

    public void setCapabilitiesNamesToUpdate(Map<String, String> capabilitiesNamesToUpdate) {
        this.capabilitiesNamesToUpdate = capabilitiesNamesToUpdate;
    }

    public Map<String, String> getRequirementsNamesToUpdate() {
        return requirementsNamesToUpdate;
    }

    public void setRequirementsNamesToUpdate(Map<String, String> requirementsNamesToUpdate) {
        this.requirementsNamesToUpdate = requirementsNamesToUpdate;
    }

    public Collection<String> getDirectives() {
        return directives;
    }

    public void setDirectives(Collection<String> directives) {
        this.directives = directives;
    }

    public UploadNodeFilterInfo getUploadNodeFilterInfo() {
        return uploadNodeFilterInfo;
    }

    public void setUploadNodeFilterInfo(UploadNodeFilterInfo uploadNodeFilterInfo) {
        this.uploadNodeFilterInfo = uploadNodeFilterInfo;
    }
}
