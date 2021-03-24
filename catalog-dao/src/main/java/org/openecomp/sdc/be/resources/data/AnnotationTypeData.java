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
package org.openecomp.sdc.be.resources.data;

import java.util.HashMap;
import java.util.Map;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.AnnotationTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class AnnotationTypeData extends GraphNode {

    private AnnotationTypeDataDefinition annotationTypeDataDefinition;

    public AnnotationTypeData() {
        super(NodeTypeEnum.AnnotationType);
        annotationTypeDataDefinition = new AnnotationTypeDataDefinition();
    }

    public AnnotationTypeData(AnnotationTypeDataDefinition annotationTypeDataDefinition) {
        super(NodeTypeEnum.AnnotationType);
        this.annotationTypeDataDefinition = annotationTypeDataDefinition;
    }

    public AnnotationTypeData(Map<String, Object> properties) {
        this();
        annotationTypeDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
        annotationTypeDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
        annotationTypeDataDefinition.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
        annotationTypeDataDefinition.setHighestVersion((boolean) properties.get(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty()));
        annotationTypeDataDefinition.setVersion((String) properties.get(GraphPropertiesDictionary.VERSION.getProperty()));
        annotationTypeDataDefinition.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
        annotationTypeDataDefinition.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
    }

    @Override
    public String getUniqueId() {
        return annotationTypeDataDefinition.getUniqueId();
    }

    @Override
    public Map<String, Object> toGraphMap() {
        Map<String, Object> map = new HashMap<>();
        addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, annotationTypeDataDefinition.getUniqueId());
        addIfExists(map, GraphPropertiesDictionary.TYPE, annotationTypeDataDefinition.getType());
        addIfExists(map, GraphPropertiesDictionary.VERSION, annotationTypeDataDefinition.getVersion());
        addIfExists(map, GraphPropertiesDictionary.IS_HIGHEST_VERSION, annotationTypeDataDefinition.isHighestVersion());
        addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, annotationTypeDataDefinition.getDescription());
        addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, annotationTypeDataDefinition.getCreationTime());
        addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, annotationTypeDataDefinition.getModificationTime());
        return map;
    }

    public void setInitialCreationProperties(String uniqueId) {
        annotationTypeDataDefinition.setUniqueId(uniqueId);
        Long creationDate = annotationTypeDataDefinition.getCreationTime();
        if (creationDate == null) {
            creationDate = System.currentTimeMillis();
        }
        annotationTypeDataDefinition.setCreationTime(creationDate);
        annotationTypeDataDefinition.setModificationTime(creationDate);
    }

    public void setUpdateProperties(AnnotationTypeDataDefinition originalDefinition) {
        annotationTypeDataDefinition.setUniqueId(originalDefinition.getUniqueId());
        annotationTypeDataDefinition.setCreationTime(originalDefinition.getCreationTime());
        annotationTypeDataDefinition.setModificationTime(System.currentTimeMillis());
    }

    public AnnotationTypeDataDefinition getAnnotationTypeDataDefinition() {
        return annotationTypeDataDefinition;
    }

    @Override
    public String toString() {
        return "AnnotationTypeData [annotationTypeDataDefinition=" + annotationTypeDataDefinition + "]";
    }
}
