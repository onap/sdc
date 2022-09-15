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

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class GroupTypeData extends GraphNode {

    private static final Type listType = new TypeToken<List<String>>() {
    }.getType();
    private static final Type mapType = new TypeToken<HashMap<String, String>>() {
    }.getType();
    private GroupTypeDataDefinition groupTypeDataDefinition;

    private GroupTypeData() {
        super(NodeTypeEnum.GroupType);
        groupTypeDataDefinition = new GroupTypeDataDefinition();
    }

    public GroupTypeData(GroupTypeDataDefinition groupTypeDataDefinition) {
        super(NodeTypeEnum.GroupType);
        this.groupTypeDataDefinition = groupTypeDataDefinition;
    }

    public GroupTypeData(Map<String, Object> properties) {
        this();
        groupTypeDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
        groupTypeDataDefinition.setIcon((String) properties.get(GraphPropertiesDictionary.ICON.getProperty()));
        groupTypeDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
        groupTypeDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
        groupTypeDataDefinition.setVersion((String) properties.get(GraphPropertiesDictionary.VERSION.getProperty()));
        if (properties.get(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty()) != null) {
            groupTypeDataDefinition.setHighestVersion((boolean) properties.get(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty()));
        }
        groupTypeDataDefinition.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
        List<String> members = getGson().fromJson((String) properties.get(GraphPropertiesDictionary.MEMBERS.getProperty()), listType);
        groupTypeDataDefinition.setMembers(members);
        HashMap<String, String> metatdata = getGson().fromJson((String) properties.get(GraphPropertiesDictionary.METADATA.getProperty()), mapType);
        groupTypeDataDefinition.setMetadata(metatdata);
        groupTypeDataDefinition.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
        groupTypeDataDefinition.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
    }

    @Override
    public Map<String, Object> toGraphMap() {
        Map<String, Object> map = new HashMap<>();
        addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, groupTypeDataDefinition.getUniqueId());
        addIfExists(map, GraphPropertiesDictionary.TYPE, groupTypeDataDefinition.getType());
        addIfExists(map, GraphPropertiesDictionary.NAME, groupTypeDataDefinition.getName());
        addIfExists(map, GraphPropertiesDictionary.ICON, groupTypeDataDefinition.getIcon());
        addIfExists(map, GraphPropertiesDictionary.VERSION, groupTypeDataDefinition.getVersion());
        addIfExists(map, GraphPropertiesDictionary.IS_HIGHEST_VERSION, groupTypeDataDefinition.isHighestVersion());
        addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, groupTypeDataDefinition.getDescription());
        addIfExists(map, GraphPropertiesDictionary.METADATA, groupTypeDataDefinition.getMetadata());
        addIfExists(map, GraphPropertiesDictionary.MEMBERS, groupTypeDataDefinition.getMembers());
        addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, groupTypeDataDefinition.getCreationTime());
        addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, groupTypeDataDefinition.getModificationTime());
        return map;
    }

    public GroupTypeDataDefinition getGroupTypeDataDefinition() {
        return groupTypeDataDefinition;
    }

    public void setGroupTypeDataDefinition(GroupTypeDataDefinition groupTypeDataDefinition) {
        this.groupTypeDataDefinition = groupTypeDataDefinition;
    }

    @Override
    public String toString() {
        return "GroupTypeData [groupTypeDataDefinition=" + groupTypeDataDefinition + "]";
    }

    @Override
    public String getUniqueId() {
        return this.groupTypeDataDefinition.getUniqueId();
    }
}
