/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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
package org.openecomp.sdc.be.resources.data;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.ArtifactTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class ArtifactTypeData extends GraphNode {

    private ArtifactTypeDataDefinition artifactTypeDefinition;

    public ArtifactTypeData() {
        super(NodeTypeEnum.Interface);
        artifactTypeDefinition = new ArtifactTypeDataDefinition();
    }

    public ArtifactTypeData(ArtifactTypeData artifactTypeData) {
        super(NodeTypeEnum.ArtifactType);
        artifactTypeDefinition = artifactTypeData.getArtifactTypeDataDefinition();
    }

    public ArtifactTypeData(ArtifactTypeDataDefinition artifactTypeDefinition) {
        super(NodeTypeEnum.ArtifactType);
        this.artifactTypeDefinition = artifactTypeDefinition;
    }

    public ArtifactTypeData(Map<String, Object> properties) {
        this();
        artifactTypeDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
        artifactTypeDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
        artifactTypeDefinition.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
        artifactTypeDefinition.setCreationDate((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
        artifactTypeDefinition.setLastUpdated((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
    }

    public ArtifactTypeDataDefinition getArtifactTypeDataDefinition() {
        return artifactTypeDefinition;
    }

    public void setArtifactTypeDataDefinition(ArtifactTypeDataDefinition artifactTypeDefinition) {
        this.artifactTypeDefinition = artifactTypeDefinition;
    }

    @Override
    public String getUniqueId() {
        return artifactTypeDefinition.getUniqueId();
    }

    @Override
    public Map<String, Object> toGraphMap() {
        Map<String, Object> map = new HashMap<>();
        addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, artifactTypeDefinition.getUniqueId());
        addIfExists(map, GraphPropertiesDictionary.TYPE, artifactTypeDefinition.getType());
        addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, artifactTypeDefinition.getCreationDate());
        addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, artifactTypeDefinition.getLastUpdated());
        addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, artifactTypeDefinition.getDescription());
        return map;
    }
}
