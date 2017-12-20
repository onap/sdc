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

package org.openecomp.core.tools.store;

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
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CassandraElementRepository {


  public Collection<ElementEntity> list(SessionContext context,
                                        ElementEntityContext elementContext) {
    Set<String> elementIds = getVersionElementIds(context, elementContext);

    return elementIds.stream()
        .map(elementId -> get(context, elementContext, new ElementEntity(new Id(elementId))).get())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }


  public void update(SessionContext context, ElementEntityContext elementContext,
                     ElementEntity element) {
    updateElement(context, elementContext, element);
  }


  public Optional<ElementEntity> get(SessionContext context, ElementEntityContext elementContext,
                                     ElementEntity element) {
    Row row = getElementAccessor(context).get(
        elementContext.getSpace(),
        elementContext.getItemId().getValue(),
        getVersionId(elementContext),
        element.getId().getValue()).one();

    return row == null ? Optional.empty() : Optional.of(getElementEntity(element, row));
  }


  private String getVersionId(ElementEntityContext elementContext) {
    return elementContext.getRevisionId() == null
        ? elementContext.getVersionId().getValue()
        : elementContext.getRevisionId().getValue();
  }


  private ElementAccessor getElementAccessor(SessionContext context) {
    return NoSqlDbFactory.getInstance().createInterface().getMappingManager().createAccessor
        (ElementAccessor.class);

  }

  private VersionElementsAccessor getVersionElementsAccessor() {
    return NoSqlDbFactory.getInstance().createInterface().getMappingManager().createAccessor
        (VersionElementsAccessor.class);

  }


  private void updateElement(SessionContext context, ElementEntityContext elementContext,
                             ElementEntity element) {

    if (elementContext.getRevisionId() == null) {

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
    } else {
      getElementAccessor(context).update(
          JsonUtil.object2Json(element.getInfo()),
          JsonUtil.object2Json(element.getRelations()),
          element.getData(),
          element.getSearchableData(),
          element.getVisualization(),
          elementContext.getSpace(),
          elementContext.getItemId().toString(),
          elementContext.getRevisionId().getValue(),
          element.getId().toString());
    }
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
    Row row = getVersionElementsAccessor().get(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        getVersionId(elementContext)).one();
    return row == null
        ? new HashSet<>()
        : row.getSet(VersionElementsField.ELEMENT_IDS, String.class);
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
        "UPDATE zusammen_dox.element SET parent_id=:parentId, namespace=:ns, info=:info, " +
            "relations=:rels, " +
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

    @Query("UPDATE zusammen_dox.element SET info=?, relations=?, data=?, searchable_data=?, " +
        "visualization=?" +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=?  ")
    void update(String info, String relations, ByteBuffer data, ByteBuffer searchableData,
                ByteBuffer visualization, String space, String itemId, String versionId,
                String elementId);


    @Query("SELECT parent_id, namespace, info, relations, data, searchable_data, visualization, " +
        "sub_element_ids FROM zusammen_dox.element " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? ")
    ResultSet get(String space, String itemId, String versionId, String elementId);


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


    @Query("SELECT element_ids FROM zusammen_dox.version_elements WHERE space=? AND item_id=? AND version_id=?")
    ResultSet get(String space, String itemId, String versionId);


  }

  private static final class VersionElementsField {
    private static final String ELEMENT_IDS = "element_ids";
  }
}
