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
import lombok.Getter;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

@Getter
public class ModelData extends GraphNode {

    private final String name;
    private final String uniqueId;
    private final String modelType;

    public ModelData(final String name, final String uniqueId, final String modelType) {
        super(NodeTypeEnum.Model);
        this.name = name;
        this.uniqueId = uniqueId;
        this.modelType = modelType;
    }

    public ModelData(final Map<String, Object> properties) {
        super(NodeTypeEnum.Model);
        name = (String) properties.get(GraphPropertiesDictionary.NAME.getProperty());
        uniqueId = (String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
        modelType = (String) properties.get(GraphPropertiesDictionary.MODEL_TYPE.getProperty());
    }

    @Override
    public Map<String, Object> toGraphMap() {
        final Map<String, Object> map = new HashMap<>();
        addIfExists(map, GraphPropertiesDictionary.NAME, name);
        addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);
        addIfExists(map, GraphPropertiesDictionary.MODEL_TYPE, modelType);
        return map;
    }

}