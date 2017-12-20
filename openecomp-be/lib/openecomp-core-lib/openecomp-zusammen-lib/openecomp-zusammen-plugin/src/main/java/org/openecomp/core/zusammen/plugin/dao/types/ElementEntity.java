/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.zusammen.plugin.dao.types;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ElementEntity {
  private Id id;
  private Id parentId;
  private Namespace namespace;
  private Id elementHash;
  private Info info;
  private Collection<Relation> relations = Collections.emptyList();
  private ByteBuffer data;
  private ByteBuffer searchableData;
  private ByteBuffer visualization;
  private Set<Id> subElementIds = Collections.emptySet();

  public ElementEntity(Id id) {
    this.id = id;
  }

  public Id getId() {
    return id;
  }

  public Id getParentId() {
    return parentId;
  }

  public void setParentId(Id parentId) {
    this.parentId = parentId;
  }

  public Namespace getNamespace() {
    return namespace;
  }

  public void setNamespace(Namespace namespace) {
    this.namespace = namespace;
  }

  public Info getInfo() {
    return info;
  }

  public void setInfo(Info info) {
    this.info = info;
  }

  public Collection<Relation> getRelations() {
    return relations;
  }

  public void setRelations(Collection<Relation> relations) {
    this.relations = relations;
  }

  public ByteBuffer getData() {
    return data;
  }

  public void setData(ByteBuffer data) {
    this.data = data;
  }

  public ByteBuffer getSearchableData() {
    return searchableData;
  }

  public void setSearchableData(ByteBuffer searchableData) {
    this.searchableData = searchableData;
  }

  public ByteBuffer getVisualization() {
    return visualization;
  }

  public void setVisualization(ByteBuffer visualization) {
    this.visualization = visualization;
  }

  public Set<Id> getSubElementIds() {
    return subElementIds;
  }

  public void setSubElementIds(Set<Id> subElementIds) {
    this.subElementIds = subElementIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ElementEntity that = (ElementEntity) o;

    return id.equals(that.id);
  }

  public Id getElementHash() {
    return elementHash;
  }

  public void setElementHash(Id elementHash) {
    this.elementHash = elementHash;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

}
