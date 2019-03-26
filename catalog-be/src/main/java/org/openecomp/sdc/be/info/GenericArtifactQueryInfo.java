/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.info;

import java.util.List;

public class GenericArtifactQueryInfo {

    private List<String> fields;
    private String parentId;
    private String artifactUniqueId;

    public GenericArtifactQueryInfo() {
    }

    public GenericArtifactQueryInfo(List<String> fields, String parentId, String artifactUniqueId) {
        this.fields = fields;
        this.parentId = parentId;
        this.artifactUniqueId = artifactUniqueId;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setArtifactUniqueId(String artifactUniqueId) {
        this.artifactUniqueId = artifactUniqueId;
    }

    public List<String> getFields() {
        return fields;
    }

    public String getParentId() {
        return parentId;
    }

    public String getArtifactUniqueId() {
        return artifactUniqueId;
    }
}
