/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NodeTypeInfoToUpdateArtifacts {

    private String nodeName;
    private Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle;

    NodeTypeInfoToUpdateArtifacts() {}

    public NodeTypeInfoToUpdateArtifacts(String nodeName,
            Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle) {
        super();
        this.nodeName = nodeName;
        this.nodeTypesArtifactsToHandle = nodeTypesArtifactsToHandle;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> getNodeTypesArtifactsToHandle() {
        return nodeTypesArtifactsToHandle;
    }

    public void setNodeTypesArtifactsToHandle(
            Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle) {
        this.nodeTypesArtifactsToHandle = nodeTypesArtifactsToHandle;
    }


}
