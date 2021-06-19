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
package org.openecomp.sdc.be.resources.data;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

@Getter
@Setter
@ToString
public abstract class CategoryData extends GraphNode {

    private String name;
    private String normalizedName;
    private String uniqueId;

    protected CategoryData(NodeTypeEnum label) {
        super(label);
    }

    protected CategoryData(String name, String normalizedName, NodeTypeEnum label) {
        super(label);
        this.name = name;
        this.normalizedName = normalizedName;
    }

    protected CategoryData(Map<String, Object> properties, NodeTypeEnum label) {
        super(label);
        setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
        setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
        setNormalizedName((String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty()));
    }

    protected abstract void createUniqueId();

    @Override
    public Map<String, Object> toGraphMap() {
        Map<String, Object> map = new HashMap<>();
        addIfExists(map, GraphPropertiesDictionary.NAME, name);
        addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, uniqueId);
        addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, normalizedName);
        return map;
    }

    @Override
    public String getUniqueIdKey() {
        return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
    }

}
