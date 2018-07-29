package org.openecomp.sdc.be.resources.data;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.AnnotationTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.util.HashMap;
import java.util.Map;

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
        annotationTypeDataDefinition
                .setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
        annotationTypeDataDefinition.setType((String) properties.get(GraphPropertiesDictionary.TYPE.getProperty()));
        annotationTypeDataDefinition
                .setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
        annotationTypeDataDefinition.setHighestVersion(
                (boolean) properties.get(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty()));
        annotationTypeDataDefinition.setVersion((String) properties.get(GraphPropertiesDictionary.VERSION.getProperty()));
        annotationTypeDataDefinition
                .setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
        annotationTypeDataDefinition
                .setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
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
