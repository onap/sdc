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

package org.openecomp.core.zusammen.plugin.dao.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import com.google.gson.reflect.TypeToken;
import org.openecomp.core.zusammen.plugin.dao.ElementRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CassandraElementRepository implements ElementRepository {

  private static final String VERSION_ELEMENT_NOT_EXIST_ERROR_MSG =
      "List version elements error: " +
          "element %s, which appears as an element of item %s version %s, does not exist";

  @Override
  public Collection<ElementEntity> list(SessionContext context,
                                        ElementEntityContext elementContext) {
    Set<String> elementIds = getVersionElementIds(context, elementContext);

    Collection<ElementEntity> elements = new ArrayList<>();
    for (String elementId : elementIds) {
      elements.add(get(context, elementContext, new ElementEntity(new Id(elementId)))
          .orElseThrow(
              () -> new IllegalStateException(String.format(VERSION_ELEMENT_NOT_EXIST_ERROR_MSG,
                  elementId, elementContext.getItemId().getValue(),
                  getVersionId(elementContext)))));
    }
    return elements;
  }

  @Override
  public void create(SessionContext context, ElementEntityContext elementContext,
                     ElementEntity element) {
    createElement(context, elementContext, element);
    addElementToParent(context, elementContext, element);
  }

  @Override
  public void update(SessionContext context, ElementEntityContext elementContext,
                     ElementEntity element) {
    updateElement(context, elementContext, element);
  }

  @Override
  public void delete(SessionContext context, ElementEntityContext elementContext,
                     ElementEntity element) {
    removeElementFromParent(context, elementContext, element);
    deleteElement(context, elementContext, element);
  }

  @Override
  public Optional<ElementEntity> get(SessionContext context, ElementEntityContext elementContext,
                                     ElementEntity element) {
    Row row = getElementAccessor(context).get(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        getVersionId(elementContext),
        element.getId().toString()).one();

    return row == null ? Optional.empty() : Optional.of(getElementEntity(element, row));
  }

  @Override
  public void createNamespace(SessionContext context, ElementEntityContext elementContext,
                              ElementEntity element) {
    getElementNamespaceAccessor(context).create(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        element.getId().toString(),
        element.getNamespace().toString());
  }

  @Override
  public boolean checkHealth(SessionContext context) {
    ResultSet resultSet = getVersionElementsAccessor(context).checkHealth();
    return resultSet.getColumnDefinitions().contains("element_ids");
  }

  private String getVersionId(ElementEntityContext elementContext) {
    return elementContext.getChangeRef() == null
        ? elementContext.getVersionId().toString()
        : elementContext.getChangeRef();
  }

  private ElementNamespaceAccessor getElementNamespaceAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, ElementNamespaceAccessor.class);
  }

  private ElementAccessor getElementAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, ElementAccessor.class);
  }

  private VersionElementsAccessor getVersionElementsAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, VersionElementsAccessor.class);
  }

  private void createElement(SessionContext context, ElementEntityContext elementContext,
                             ElementEntity element) {
    Set<String> subElementIds =
        element.getSubElementIds().stream().map(Id::toString).collect(Collectors.toSet());
    String versionId = getVersionId(elementContext);

    getElementAccessor(context).create(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        versionId,
        element.getId().toString(),
        element.getParentId().toString(),
        element.getNamespace().toString(),
        JsonUtil.object2Json(element.getInfo()),
        JsonUtil.object2Json(element.getRelations()),
        element.getData(),
        element.getSearchableData(),
        element.getVisualization(),
        subElementIds);

    getVersionElementsAccessor(context).addElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        versionId);
  }

  private void updateElement(SessionContext context, ElementEntityContext elementContext,
                             ElementEntity element) {
    getElementAccessor(context).update(
        JsonUtil.object2Json(element.getInfo()),
        JsonUtil.object2Json(element.getRelations()),
        element.getData(),
        element.getSearchableData(),
        element.getVisualization(),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getId().toString());
  }

  private void deleteElement(SessionContext context, ElementEntityContext elementContext,
                             ElementEntity element) {
    String versionId = getVersionId(elementContext);

    getElementAccessor(context).delete(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        versionId,
        element.getId().toString());

    getVersionElementsAccessor(context).removeElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        versionId);
  }

  private void addElementToParent(SessionContext context, ElementEntityContext elementContext,
                                  ElementEntity element) {
    getElementAccessor(context).addSubElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        getVersionId(elementContext),
        element.getParentId().toString());
  }

  private void removeElementFromParent(SessionContext context, ElementEntityContext elementContext,
                                       ElementEntity element) {
    if (element.getParentId() == null) {
      return;
    }
    getElementAccessor(context).removeSubElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        getVersionId(elementContext),
        element.getParentId().toString());
  }

  private ElementEntity getElementEntity(ElementEntity element, Row row) {
    element.setNamespace(getNamespace(row.getString(ElementField.NAMESPACE)));
    element.setParentId(new Id(row.getString(ElementField.PARENT_ID)));
    element.setInfo(json2Object(row.getString(ElementField.INFO), Info.class));
    element.setRelations(
        json2Object(row.getString(ElementField.RELATIONS), new TypeToken<ArrayList<Relation>>() {
        }.getType()));
    element.setData(row.getBytes(ElementField.DATA));
    element.setSearchableData(row.getBytes(ElementField.SEARCHABLE_DATA));
    element.setVisualization(row.getBytes(ElementField.VISUALIZATION));
    element.setSubElementIds(row.getSet(ElementField.SUB_ELEMENT_IDS, String.class)
        .stream().map(Id::new).collect(Collectors.toSet()));
    return element;
  }

  private Namespace getNamespace(String namespaceStr) {
    Namespace namespace = new Namespace();
    if (namespaceStr != null) {
      namespace.setValue(namespaceStr);
    }
    return namespace;
  }

  private static <T> T json2Object(String json, Type typeOfT) {
    return json == null ? null : JsonUtil.json2Object(json, typeOfT);
  }

  private Set<String> getVersionElementIds(SessionContext context,
                                           ElementEntityContext elementContext) {
    Row row = getVersionElementsAccessor(context).get(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        getVersionId(elementContext)).one();
    return row == null
        ? new HashSet<>()
        : row.getSet(CassandraElementRepository.VersionElementsField.ELEMENT_IDS, String.class);
  }

  /*
CREATE TABLE IF NOT EXISTS element_namespace (
	space text,
	item_id text,
	element_id text,
	namespace text,
	PRIMARY KEY (( space, item_id, element_id ))
);
   */
  @Accessor
  interface ElementNamespaceAccessor {
    @Query(
        "UPDATE element_namespace SET namespace=:ns " +
            "WHERE space=:space AND item_id=:item AND element_id=:id ")
    void create(@Param("space") String space,
                @Param("item") String itemId,
                @Param("id") String elementId,
                @Param("ns") String namespace);
  }

  /*
CREATE TABLE IF NOT EXISTS element (
	space text,
	item_id text,
	version_id text,
	element_id text,
	parent_id text,
	namespace text,
	info text,
	relations text,
	data blob,
	searchable_data blob,
	visualization blob,
	sub_element_ids set<text>,
	PRIMARY KEY (( space, item_id, version_id, element_id ))
);
   */
  @Accessor
  interface ElementAccessor {
    @Query(
        "UPDATE element SET parent_id=:parentId, namespace=:ns, info=:info, relations=:rels, " +
            "data=:data, searchable_data=:searchableData, visualization=:visualization, " +
            "sub_element_ids=sub_element_ids+:subs " +
            "WHERE space=:space AND item_id=:item AND version_id=:ver AND element_id=:id ")
    void create(@Param("space") String space,
                @Param("item") String itemId,
                @Param("ver") String versionId,
                @Param("id") String elementId,
                @Param("parentId") String parentElementId,
                @Param("ns") String namespace,
                @Param("info") String info,
                @Param("rels") String relations,
                @Param("data") ByteBuffer data,
                @Param("searchableData") ByteBuffer searchableData,
                @Param("visualization") ByteBuffer visualization,
                @Param("subs") Set<String> subElementIds);

    @Query("UPDATE element SET info=?, relations=?, data=?, searchable_data=?, visualization=?" +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=?  ")
    void update(String info, String relations, ByteBuffer data, ByteBuffer searchableData,
                ByteBuffer visualization, String space, String itemId, String versionId,
                String elementId);

    @Query("DELETE FROM element WHERE space=? AND item_id=? AND version_id=? AND element_id=?")
    void delete(String space, String itemId, String versionId, String elementId);

    @Query("SELECT parent_id, namespace, info, relations, data, searchable_data, visualization, " +
        "sub_element_ids FROM element " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? ")
    ResultSet get(String space, String itemId, String versionId, String elementId);

    @Query("UPDATE element SET sub_element_ids=sub_element_ids+? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=?  ")
    void addSubElements(Set<String> subElementIds, String space, String itemId, String versionId,
                        String elementId);

    @Query("UPDATE element SET sub_element_ids=sub_element_ids-? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=? ")
    void removeSubElements(Set<String> subElementIds, String space, String itemId, String versionId,
                           String elementId);
  }

  private static final class ElementField {
    private static final String NAMESPACE = "namespace";
    private static final String PARENT_ID = "parent_id";
    private static final String INFO = "info";
    private static final String RELATIONS = "relations";
    private static final String DATA = "data";
    private static final String SEARCHABLE_DATA = "searchable_data";
    private static final String VISUALIZATION = "visualization";
    private static final String SUB_ELEMENT_IDS = "sub_element_ids";
  }

  /*
  CREATE TABLE IF NOT EXISTS version_elements (
    space text,
    item_id text,
    version_id text,
    element_ids set<text>,
    PRIMARY KEY (( space, item_id, version_id ))
  );
   */
  @Accessor
  interface VersionElementsAccessor {

    @Query("UPDATE version_elements SET element_ids=element_ids+? " +
        "WHERE space=? AND item_id=? AND version_id=?")
    void addElements(Set<String> elementIds, String space, String itemId, String versionId);

    @Query("UPDATE version_elements SET element_ids=element_ids-? " +
        "WHERE space=? AND item_id=? AND version_id=?")
    void removeElements(Set<String> elementIds, String space, String itemId, String versionId);

    @Query("SELECT element_ids FROM version_elements WHERE space=? AND item_id=? AND version_id=?")
    ResultSet get(String space, String itemId, String versionId);

    @Query("SELECT element_ids FROM version_elements LIMIT 1")
    ResultSet checkHealth();
  }

  private static final class VersionElementsField {
    private static final String ELEMENT_IDS = "element_ids";
  }
}
