package org.openecomp.core.tools.store.zusammen.datatypes;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * CREATE TABLE zusammen_dox.element (
 * space text,
 * item_id text,
 * version_id text,
 * element_id text,
 * data blob,
 * info text,
 * namespace text,
 * parent_id text,
 * relations text,
 * searchable_data blob,
 * sub_element_ids set<text>,
 * visualization blob,
 * PRIMARY KEY ((space, item_id, version_id, element_id))
 * )
 */
@Table(
        keyspace = "zusammen_dox",
        name = "version"
)
public class ElementEntity {
    @Column( name = "space" )
    @PartitionKey(0)
    private String space;

    @Column(   name = "item_id" )
    @PartitionKey(1)
    private String itemId;

    @Column(  name = "version_id" )
    @PartitionKey(2)
    private String versionId;

    @Column(name = "element_id")
    @PartitionKey(3)
    private String element_id;

    @Column(name = "data")
    private ByteBuffer data;

    @Column(name = "info")
    private String info;

    @Column(name = "namespace")
    private String namespace;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "relations")
    private String relations;

    @Column(name = "searchable_data")
    private ByteBuffer searchableData;

    @Column(name = "sub_element_ids")
    private Set<String> subElementIds;


    @Column(name = "visualization")
    private ByteBuffer visualization;

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getElement_id() {
        return element_id;
    }

    public void setElement_id(String element_id) {
        this.element_id = element_id;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRelations() {
        return relations;
    }

    public void setRelations(String relations) {
        this.relations = relations;
    }

    public ByteBuffer getSearchableData() {
        return searchableData;
    }

    public void setSearchableData(ByteBuffer searchableData) {
        this.searchableData = searchableData;
    }

    public Set<String> getSubElementIds() {
        return subElementIds;
    }

    public void setSubElementIds(Set<String> subElementIds) {
        this.subElementIds = subElementIds;
    }

    public ByteBuffer getVisualization() {
        return visualization;
    }

    public void setVisualization(ByteBuffer visualization) {
        this.visualization = visualization;
    }
}
