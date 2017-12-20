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

package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

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
import org.openecomp.core.zusammen.plugin.ZusammenPluginConstants;
import org.openecomp.core.zusammen.plugin.dao.ElementRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ElementRepositoryImpl implements ElementRepository {

  @Override
  public Map<Id, Id> listIds(SessionContext context, ElementEntityContext elementContext) {

    if (elementContext.getRevisionId() == null) {
      String revisionId = calculateLastRevisionId(context, elementContext);
      if (revisionId == null) {
        return new HashMap<>();
      }

      elementContext.setRevisionId(new Id(revisionId));
    }
    return getVersionElementIds(context, elementContext).entrySet().stream().collect(Collectors
        .toMap(entry -> new Id(entry.getKey()), entry -> new Id(entry.getValue())));

   /* return getVersionElementIds(context, elementContext).stream()
        .map(Id::new)
        .collect(Collectors.toList());*/

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

    Id elementRevisionId = getElementRevision(context, elementContext, element.getId());
    if (elementRevisionId.equals(elementContext.getRevisionId())) {
      updateElement(context, elementContext, element);
    } else {
      createElement(context, elementContext, element);
    }
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
    String revisionId = calculateElementRevisionId(context, elementContext, element);
    if (revisionId == null) {
      return Optional.empty();
    }

    Row row = getElementAccessor(context).get(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getId().toString(),
        revisionId).one();

    return row == null ? Optional.empty() : Optional.of(getElementEntity(element, row));
  }

  @Override
  public Optional<ElementEntity> getDescriptor(SessionContext context,
                                               ElementEntityContext elementContext,
                                               ElementEntity element) {
    String revisionId = calculateElementRevisionId(context, elementContext, element);
    if (revisionId == null) {
      return Optional.empty();
    }

    Row row = getElementAccessor(context).getDescriptor(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getId().toString(),
        revisionId).one();

    return row == null ? Optional.empty() : Optional.of(getElementEntityDescriptor(element, row));
  }

  @Override
  public void createNamespace(SessionContext context, ElementEntityContext elementContext,
                              ElementEntity element) {
    getElementNamespaceAccessor(context).create(elementContext.getItemId().toString(),
        element.getId().toString(),
        element.getNamespace().toString());
  }

  @Override
  public Optional<Id> getHash(SessionContext context, ElementEntityContext elementContext,
                              ElementEntity element) {
    String revisionId = calculateElementRevisionId(context, elementContext, element);
    if (revisionId == null) {
      return Optional.empty();
    }

    Row row = getElementAccessor(context).getHash(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getId().toString(),
        revisionId).one();

    return row == null ? Optional.empty() : Optional.of(getElementHash(row));
  }

  private String calculateElementRevisionId(SessionContext context,
                                            ElementEntityContext elementContext,
                                            ElementEntity element) {

    if (elementContext.getSpace().equals(ZusammenPluginConstants.PUBLIC_SPACE)) {

      String versionRevision;
      if (elementContext.getRevisionId() == null) {
        versionRevision = calculateLastRevisionId(context, elementContext);
      } else {
        versionRevision = elementContext.getRevisionId().getValue();
      }

      elementContext.setRevisionId(new Id(versionRevision));
      Map<String, String> elementIds = getVersionElementIds(context, elementContext);
      if (elementIds.containsKey(element.getId().getValue())) {
        return elementIds.get(element.getId().getValue());
      } else {
        return null;
      }

    } else {
      return Id.ZERO.getValue();
    }
  }

  private String calculateLastRevisionId(SessionContext context, ElementEntityContext
      elementContext) {
    List<Row> rows = getVersionElementsAccessor(context).listRevisions(elementContext.getSpace(),
        elementContext
            .getItemId().toString(), elementContext.getVersionId().toString()).all();
    if (rows == null || rows.size() == 0) {
      return null;
    }
    rows.sort((o1, o2) -> o1.getDate(VersionElementsField.PUBLISH_TIME)
        .after(o2.getDate(VersionElementsField.PUBLISH_TIME)) ? -1 : 1);
    return rows.get(0).getString(VersionElementsField.REVISION_ID);
  }

  /*private static String getVersionId(ElementEntityContext elementContext) {
    return elementContext.getRevisionId() == null
        ? elementContext.getVersionId().toString()
        : elementContext.getRevisionId().getValue();
  }*/

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
    createElementRow(context, elementContext, element);

    Map<String, String> elementIds = new TreeMap<>();
    elementIds.put(element.getId().toString(), elementContext.getRevisionId().getValue());
    getVersionElementsAccessor(context).addElements(
        elementIds,
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue());
  }

  private void createElementRow(SessionContext context, ElementEntityContext elementContext,
                                ElementEntity element) {
    Set<String> subElementIds =
        element.getSubElementIds().stream().map(Id::toString).collect(Collectors.toSet());

    getElementAccessor(context).create(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getId().toString(),
        elementContext.getRevisionId().getValue(),
        element.getParentId() == null ? null : element.getParentId().toString(),
        element.getNamespace() == null ? null : element.getNamespace().toString(),
        JsonUtil.object2Json(element.getInfo()),
        JsonUtil.object2Json(element.getRelations()),
        element.getData(),
        element.getSearchableData(),
        element.getVisualization(),
        subElementIds,
        element.getElementHash().getValue());
  }

  private void updateElement(SessionContext context, ElementEntityContext elementContext,
                             ElementEntity element) {


    if (element.getParentId() == null) {
      getElementAccessor(context).update(
          JsonUtil.object2Json(element.getInfo()),
          JsonUtil.object2Json(element.getRelations()),
          element.getData(),
          element.getSearchableData(),
          element.getVisualization(),
          element.getElementHash().getValue(),
          elementContext.getSpace(),
          elementContext.getItemId().toString(),
          elementContext.getVersionId().toString(),
          element.getId().toString(),
          elementContext.getRevisionId().getValue());
    } else {
      getElementAccessor(context).update(
          JsonUtil.object2Json(element.getInfo()),
          JsonUtil.object2Json(element.getRelations()),
          element.getData(),
          element.getSearchableData(),
          element.getVisualization(),
          element.getElementHash().getValue(),
          element.getParentId().getValue(),
          elementContext.getSpace(),
          elementContext.getItemId().toString(),
          elementContext.getVersionId().toString(),
          element.getId().getValue(),
          elementContext.getRevisionId().getValue());
    }

    Map<String, String> elementIds = new TreeMap<>();
    elementIds.put(element.getId().getValue(), elementContext.getRevisionId().getValue());
    getVersionElementsAccessor(context).addElements(
        elementIds,
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue());
  }

  private void deleteElement(SessionContext context, ElementEntityContext elementContext,
                             ElementEntity element) {


    getElementAccessor(context).delete(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getId().toString(),
        elementContext.getRevisionId().getValue());

    getVersionElementsAccessor(context).removeElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        elementContext.getRevisionId().getValue());
  }

  private void addElementToParent(SessionContext context, ElementEntityContext elementContext,
                                  ElementEntity element) {
    if (element.getParentId() == null) {
      return;
    }


    getElementAccessor(context).addSubElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getParentId().toString(),
        elementContext.getRevisionId().getValue());

    Map<String, String> elementIds = new TreeMap<>();
    elementIds.put(element.getParentId().toString(), elementContext.getRevisionId().getValue());
    getVersionElementsAccessor(context).addElements(elementIds, elementContext.getSpace(),
        elementContext.getItemId().getValue(), elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue());
  }

  private void removeElementFromParent(SessionContext context, ElementEntityContext elementContext,
                                       ElementEntity element) {

    if (element.getParentId() == null) {
      return;
    }

    Optional<ElementEntity> parentElement =
        get(context, elementContext, new ElementEntity(element.getParentId()));
    if (!parentElement.isPresent()) {
      return;
    }
    getElementAccessor(context).removeSubElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getParentId().toString(),
        elementContext.getRevisionId().getValue());

    getVersionElementsAccessor(context)
        .removeElements(Collections.singleton(element.getId().toString()),
            elementContext.getSpace(),
            elementContext.getItemId().getValue(), elementContext.getVersionId().getValue(),
            elementContext.getRevisionId().getValue());

    Map<String, String> elementIds = new TreeMap<>();
    elementIds.put(element.getParentId().toString(), elementContext.getRevisionId().getValue());
    getVersionElementsAccessor(context).addElements(elementIds, elementContext.getSpace(),
        elementContext.getItemId().getValue(), elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue());
  }

  static ElementEntity getElementEntityDescriptor(ElementEntity element, Row row) {
    element.setNamespace(getNamespace(row.getString(ElementField.NAMESPACE)));
    element.setParentId(getParentId(row.getString(ElementField.PARENT_ID)));
    element.setInfo(json2Object(row.getString(ElementField.INFO), Info.class));
    element.setRelations(
        json2Object(row.getString(ElementField.RELATIONS), new TypeToken<ArrayList<Relation>>() {
        }.getType()));

    element.setSubElementIds(row.getSet(ElementField.SUB_ELEMENT_IDS, String.class)
        .stream().map(Id::new).collect(Collectors.toSet()));
    return element;
  }

  static ElementEntity getElementEntity(ElementEntity element, Row row) {
    getElementEntityDescriptor(element, row);

    element.setData(row.getBytes(ElementField.DATA));
    element.setSearchableData(row.getBytes(ElementField.SEARCHABLE_DATA));
    element.setVisualization(row.getBytes(ElementField.VISUALIZATION));
    element.setElementHash(new Id(row.getString(ElementField.ELEMENT_HASH)));
    return element;
  }

  private Id getElementHash(Row row) {
    return new Id(row.getString(ElementField.ELEMENT_HASH));
  }

  private static Id getParentId(String parentIdStr) {
    return parentIdStr == null ? null : new Id(parentIdStr);
  }

  private static Namespace getNamespace(String namespaceStr) {
    Namespace namespace = new Namespace();
    if (namespaceStr != null) {
      namespace.setValue(namespaceStr);
    }
    return namespace;
  }

  private static <T> T json2Object(String json, Type typeOfT) {
    return json == null ? null : JsonUtil.json2Object(json, typeOfT);
  }

  private Map<String, String> getVersionElementIds(SessionContext context,
                                                   ElementEntityContext elementContext) {
    Row row = getVersionElementsAccessor(context).get(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue()).one();
    return row == null
        ? new HashMap<>()
        : row.getMap(ElementRepositoryImpl.VersionElementsField.ELEMENT_IDS, String.class, String
            .class);
  }

  private Id getElementRevision(SessionContext context, ElementEntityContext elementContext
      , Id elementId) {
    Map<Id, Id> versionElementIds =
        listIds(context, new ElementEntityContext
            (elementContext.getSpace(), elementContext.getItemId(), elementContext.getVersionId(),
                elementContext.getRevisionId()));
    return versionElementIds.get(elementId);

  }


  /*
CREATE TABLE IF NOT EXISTS element_namespace (
	item_id text,
	element_id text,
	namespace text,
	PRIMARY KEY (( item_id, element_id ))
);
   */
  @Accessor
  interface ElementNamespaceAccessor {
    @Query("UPDATE element_namespace SET namespace=:ns " +
        "WHERE item_id=:item AND element_id=:id ")
    void create(@Param("item") String itemId,
                @Param("id") String elementId,
                @Param("ns") String namespace);
  }

  @Accessor
  interface ElementAccessor {
    @Query(
        "UPDATE element SET parent_id=:parentId, namespace=:ns, info=:info, relations=:rels, " +
            "data=:data, searchable_data=:searchableData, visualization=:visualization, " +
            "sub_element_ids=sub_element_ids+:subs , element_hash=:elementHash " +
            " WHERE space=:space AND item_id=:item AND version_id=:ver AND element_id=:id AND " +
            "revision_id=:rev ")
    void create(@Param("space") String space,
                @Param("item") String itemId,
                @Param("ver") String versionId,
                @Param("id") String elementId,
                @Param("rev") String revisionId,
                @Param("parentId") String parentElementId,
                @Param("ns") String namespace,
                @Param("info") String info,
                @Param("rels") String relations,
                @Param("data") ByteBuffer data,
                @Param("searchableData") ByteBuffer searchableData,
                @Param("visualization") ByteBuffer visualization,
                @Param("subs") Set<String> subElementIds,
                @Param("elementHash") String elementHash);


    @Query("UPDATE element SET info=?, relations=?, data=?, searchable_data=?, visualization=? ," +
        "element_hash=? , parent_id=? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=?  ")
    void update(String info, String relations, ByteBuffer data, ByteBuffer searchableData,
                ByteBuffer visualization, String elementHash, String parentId, String space, String
                    itemId, String
                    versionId, String elementId, String revisionId);

    @Query("UPDATE element SET info=?, relations=?, data=?, searchable_data=?, visualization=? ," +
        "element_hash=? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=?  ")
    void update(String info, String relations, ByteBuffer data, ByteBuffer searchableData,
                ByteBuffer visualization, String elementHash, String space, String
                    itemId, String
                    versionId, String elementId, String revisionId);

    @Query(
        "DELETE FROM element WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=? ")
    void delete(String space, String itemId, String versionId, String elementId, String revisionId);

    @Query("SELECT parent_id, namespace, info, relations, data, searchable_data, visualization, " +
        "sub_element_ids,element_hash FROM element " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=? ")
    ResultSet get(String space, String itemId, String versionId, String elementId, String
        revisionId);

    @Query("SELECT parent_id, namespace, info, relations, sub_element_ids FROM element " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=? ")
    ResultSet getDescriptor(String space, String itemId, String versionId, String elementId,
                            String revisionId);

    @Query("UPDATE element SET sub_element_ids=sub_element_ids+? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=?  ")
    void addSubElements(Set<String> subElementIds, String space, String itemId, String versionId,
                        String elementId, String revisionId);

    @Query("UPDATE element SET sub_element_ids=sub_element_ids-? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=? ")
    void removeSubElements(Set<String> subElementIds, String space, String itemId, String versionId,
                           String elementId, String revisionId);

    @Query("SELECT element_hash FROM element " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id=? ")
    ResultSet getHash(String space, String itemId, String versionId, String elementId, String
        revisionId);
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
    private static final String ELEMENT_HASH = "element_hash";
  }

  @Accessor
  interface VersionElementsAccessor {

    @Query("UPDATE version_elements SET element_ids=element_ids+ ? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    void addElements(Map<String, String> elementIds, String space, String itemId, String versionId,
                     String versionRevisionId);

    @Query("UPDATE version_elements SET element_ids=element_ids-? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=?")
    void removeElements(Set<String> elementIds, String space, String itemId, String versionId,
                        String revisionId);

    @Query(
        "SELECT element_ids FROM version_elements WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    ResultSet get(String space, String itemId, String versionId, String revisionId);

    @Query(
        "SELECT revision_id,publish_time FROM version_elements WHERE space=? AND item_id=? AND " +
            "version_id=? ")
    ResultSet listRevisions(String space, String itemId, String versionId);

  }

  private static final class VersionElementsField {
    private static final String ELEMENT_IDS = "element_ids";
    private static final String REVISION_ID = "revision_id";
    private static final String PUBLISH_TIME = "publish_time";
  }
}
