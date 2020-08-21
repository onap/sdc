/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Network Service Descriptor
 */
public class Nsd {

    public static final String DESIGNER_PROPERTY = "designer";
    public static final String VERSION_PROPERTY = "version";
    public static final String NAME_PROPERTY = "name";
    public static final String INVARIANT_ID_PROPERTY = "invariant_id";

    private String designer;
    private String version;
    private String name;
    private String invariantId;
    private byte[] contents;
    private List<String> artifactReferences = new ArrayList<>();

    public String getDesigner() {
        return designer;
    }

    public void setDesigner(String designer) {
        this.designer = designer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvariantId() {
        return invariantId;
    }

    public void setInvariantId(String invariantId) {
        this.invariantId = invariantId;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    public List<String> getArtifactReferences() {
        return artifactReferences;
    }

    public void setArtifactReferences(List<String> artifactReferences) {
        this.artifactReferences = artifactReferences;
    }
}
