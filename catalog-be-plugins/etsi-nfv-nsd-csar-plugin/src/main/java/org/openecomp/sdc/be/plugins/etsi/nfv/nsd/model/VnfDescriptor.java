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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Virtual Network Function Descriptor
 */
public class VnfDescriptor {

    private String name;
    private String vnfdFileName;
    private String nodeType;
    private Map<String, byte[]> definitionFiles = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, byte[]> getDefinitionFiles() {
        return definitionFiles;
    }

    public void setDefinitionFiles(Map<String, byte[]> definitionFiles) {
        this.definitionFiles = definitionFiles;
    }

    public String getVnfdFileName() {
        return vnfdFileName;
    }

    public void setVnfdFileName(String vnfdFileName) {
        this.vnfdFileName = vnfdFileName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

}
