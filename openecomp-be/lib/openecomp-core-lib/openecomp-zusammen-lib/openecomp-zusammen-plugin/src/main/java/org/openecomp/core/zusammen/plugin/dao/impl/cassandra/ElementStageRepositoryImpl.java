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
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.zusammen.plugin.dao.ElementStageRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementStageRepositoryImpl implements ElementStageRepository {

  @Override
  public Collection<ElementEntity> listIds(SessionContext context,
                                           ElementEntityContext elementContext) {
    return getElements(getStageElementIds(context, elementContext));
  }

  @Override
  public Collection<ElementEntity> listConflictedIds(SessionContext context,
                                                     ElementEntityContext elementContext) {
    return getElements(getConflictedElementIds(context, elementContext));
  }

  @Override
  public void create(SessionContext context, ElementEntityContext elementContext,
                     StageEntity<ElementEntity> elementStage) {
    createElement(context, elementContext, elementStage);
    addElementToParent(context, elementContext, elementStage.getEntity());
  }

  @Override
  public void markAsNotConflicted(SessionContext context, ElementEntityContext elementContext,
                                  ElementEntity element, Action action) {
    getElementStageAccessor(context).updateState(action, false,
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getId().toString());

    getStageElementsAccessor(context).removeConflictElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        elementContext.getRevisionId().getValue());
  }

  @Override
  public void markAsNotConflicted(SessionContext context, ElementEntityContext elementContext,
                                  ElementEntity element) {
    getElementStageAccessor(context).markAsNotConflicted(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getId().toString());

    getStageElementsAccessor(context).removeConflictElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        elementContext.getRevisionId().getValue());
  }

  @Override
  public void update(SessionContext context, ElementEntityContext elementContext,
                     ElementEntity element, Action action, boolean conflicted) {
    getElementStageAccessor(context).update(
        JsonUtil.object2Json(element.getInfo()),
        JsonUtil.object2Json(element.getRelations()),
        element.getData(),
        element.getSearchableData(),
        element.getVisualization(),
        element.getElementHash().getValue(),
        action,
        conflicted,
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        element.getId().toString());

    if (!conflicted) {
      getStageElementsAccessor(context).removeConflictElements(
          Collections.singleton(element.getId().toString()),
          elementContext.getSpace(),
          elementContext.getItemId().toString(),
          elementContext.getVersionId().toString(),
          elementContext.getRevisionId().getValue());
    }
  }

  @Override
  public void delete(SessionContext context, ElementEntityContext elementContext,
                     ElementEntity element) {
    removeElementFromParent(context, elementContext, element);
    deleteElement(context, elementContext, element);
  }

  @Override
  public Optional<StageEntity<ElementEntity>> get(SessionContext context,
                                                  ElementEntityContext elementContext,
                                                  ElementEntity element) {
    Row row = getElementStageAccessor(context).get(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getId().toString()).one();

    return row == null ? Optional.empty() : Optional.of(getStageElement(row));
  }

  @Override
  public Optional<StageEntity<ElementEntity>> getDescriptor(SessionContext context,
                                                            ElementEntityContext elementContext,
                                                            ElementEntity element) {
    Row row = getElementStageAccessor(context).getDescriptor(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getId().toString()).one();

    return row == null ? Optional.empty() : Optional.of(getStageElementDescriptor(row));
  }



  private Collection<ElementEntity> getElements(Set<String> elementIds) {
    return elementIds.stream()
        .map(id -> new ElementEntity(new Id(id)))
        .collect(Collectors.toList());
  }

  private void createElement(SessionContext context, ElementEntityContext elementContext,
                             StageEntity<ElementEntity> elementStage) {


    ElementEntity element = elementStage.getEntity();
    Set<String> subElementIds =
        element.getSubElementIds().stream().map(Id::toString).collect(Collectors.toSet());
    Set<String> conflictDependents = elementStage.getConflictDependents().stream()
        .map(conflictDependent -> conflictDependent.getId().getValue())
        .collect(Collectors.toSet());

    getElementStageAccessor(context).create(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getId().toString(),
        element.getParentId() == null ? null : element.getParentId().toString(),
        element.getNamespace() == null ? null : element.getNamespace().toString(),
        JsonUtil.object2Json(element.getInfo()),
        JsonUtil.object2Json(element.getRelations()),
        element.getData(),
        element.getSearchableData(),
        element.getVisualization(),
        subElementIds,
        element.getElementHash() == null ? null : element.getElementHash().getValue(),
        elementStage.getPublishTime(),
        elementStage.getAction(),
        elementStage.isConflicted(),
        conflictDependents);

    getStageElementsAccessor(context).add(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue());

    if (elementStage.isConflicted()) {
      getStageElementsAccessor(context).addConflictElements(
          Collections.singleton(element.getId().toString()),
          elementContext.getSpace(),
          elementContext.getItemId().toString(),
          elementContext.getVersionId().getValue(),
          elementContext.getRevisionId().getValue());
    }
  }

  private void deleteElement(SessionContext context, ElementEntityContext elementContext,
                             ElementEntity element) {


    getElementStageAccessor(context).delete(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getId().toString());

    getStageElementsAccessor(context).remove(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue());
  }

  private void addElementToParent(SessionContext context, ElementEntityContext elementContext,
                                  ElementEntity element) {
    if (element.getParentId() == null) {
      return;
    }
    getElementStageAccessor(context).addSubElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getParentId().toString());
  }

  private void removeElementFromParent(SessionContext context, ElementEntityContext elementContext,
                                       ElementEntity element) {
    if (element.getParentId() == null) {
      return;
    }
    getElementStageAccessor(context).removeSubElements(
        Collections.singleton(element.getId().toString()),
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        element.getParentId().toString());
  }

  private StageEntity<ElementEntity> getStageElementDescriptor(Row row) {
    return buildStageElement(ElementRepositoryImpl.getElementEntityDescriptor(
        new ElementEntity(new Id(row.getString(ElementStageField.ID))), row), row);
  }

  private StageEntity<ElementEntity> getStageElement(Row row) {
    return buildStageElement(ElementRepositoryImpl.getElementEntity(
        new ElementEntity(new Id(row.getString(ElementStageField.ID))), row), row);
  }


  private StageEntity<ElementEntity> buildStageElement(ElementEntity element, Row row) {
    StageEntity<ElementEntity> elementStage =
        new StageEntity<>(element, row.getDate(ElementStageField.PUBLISH_TIME));
    elementStage.setAction(Action.valueOf(row.getString(ElementStageField.ACTION)));
    elementStage.setConflicted(row.getBool(ElementStageField.CONFLICTED));
    elementStage.setConflictDependents(
        row.getSet(ElementStageField.CONFLICT_DEPENDENTS, String.class).stream()
            .map(conflictDependentId -> new ElementEntity(new Id(conflictDependentId)))
            .collect(Collectors.toSet()));
    return elementStage;
  }

  private Set<String> getStageElementIds(SessionContext context,
                                         ElementEntityContext elementContext) {
    Row row = getStageElementsAccessor(context).get(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue()).one();
    return row == null ? new HashSet<>()
        : row.getSet(StageElementsField.STAGE_ELEMENT_IDS, String.class);
  }

  private Set<String> getConflictedElementIds(SessionContext context,
                                              ElementEntityContext elementContext) {
    Row row = getStageElementsAccessor(context).getConflicted(
        elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue()).one();
    return row == null ? new HashSet<>()
        : row.getSet(StageElementsField.CONFLICT_ELEMENT_IDS, String.class);
  }

  private ElementStageAccessor getElementStageAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, ElementStageAccessor.class);
  }

  private StageElementsAccessor getStageElementsAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, StageElementsAccessor.class);
  }

  @Accessor
  interface ElementStageAccessor {
    @Query(
        "UPDATE element_stage SET parent_id=:parentId, namespace=:ns, info=:info, relations=:rels, " +
            "data=:data, searchable_data=:searchableData, visualization=:visualization, " +
            "publish_time=:publishTime, action=:action, " +
            "conflicted=:conflicted, conflict_dependent_ids=:conflictDependents, " +
            "sub_element_ids=sub_element_ids+:subs, element_hash=:elementHash " +
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
                @Param("subs") Set<String> subElementIds,
                @Param("elementHash") String elementHash,
                @Param("publishTime") Date publishTime,
                @Param("action") Action action,
                @Param("conflicted") boolean conflicted,
                @Param("conflictDependents") Set<String> conflictDependents);

    @Query("UPDATE element_stage SET info=?, relations=?, data=?, searchable_data=?, " +
        "visualization=?,element_hash=?, action=?, conflicted=? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=?  ")
    void update(String info, String relations, ByteBuffer data, ByteBuffer searchableData,
                ByteBuffer visualization, String elementHash, Action action, boolean conflicted,
                String space,
                String itemId, String versionId, String elementId);

    @Query("UPDATE element_stage SET action=?, conflicted=? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=?  ")
    void updateState(Action action, boolean conflicted, String space, String itemId,
                     String versionId, String elementId);

    @Query("UPDATE element_stage SET conflicted=false " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=?  ")
    void markAsNotConflicted(String space, String itemId, String versionId, String elementId);

    @Query(
        "DELETE FROM element_stage WHERE space=? AND item_id=? AND version_id=? AND element_id=?")
    void delete(String space, String itemId, String versionId, String elementId);

    @Query("SELECT element_id, parent_id, namespace, info, relations, data, searchable_data, " +
        "visualization, sub_element_ids,element_hash, publish_time, action, " +
        "conflicted, conflict_dependent_ids FROM element_stage " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? ")
    ResultSet get(String space, String itemId, String versionId, String elementId);

    @Query("SELECT element_id, parent_id, namespace, info, relations, " +
        "sub_element_ids, publish_time, action, conflicted, conflict_dependent_ids " +
        "FROM element_stage WHERE space=? AND item_id=? AND version_id=? AND element_id=? ")
    ResultSet getDescriptor(String space, String itemId, String versionId, String elementId);

    @Query("UPDATE element_stage SET sub_element_ids=sub_element_ids+? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=?  ")
    void addSubElements(Set<String> subElementIds, String space, String itemId, String versionId,
                        String elementId);

    @Query("UPDATE element_stage SET sub_element_ids=sub_element_ids-? " +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=? ")
    void removeSubElements(Set<String> subElementIds, String space, String itemId, String versionId,
                           String elementId);
  }

  private static final class ElementStageField {
    private static final String ID = "element_id";
    private static final String PUBLISH_TIME = "publish_time";
    private static final String ACTION = "action";
    private static final String CONFLICTED = "conflicted";
    private static final String CONFLICT_DEPENDENTS = "conflict_dependent_ids";
  }

  @Accessor
  interface StageElementsAccessor {

    @Query("UPDATE version_elements SET stage_element_ids=stage_element_ids+? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    void add(Set<String> elementIds, String space, String itemId, String versionId, String
        revisionId);

    @Query("UPDATE version_elements SET stage_element_ids=stage_element_ids-? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    void remove(Set<String> elementIds, String space, String itemId, String versionId, String
        revisionId);

    @Query("SELECT stage_element_ids FROM version_elements " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=?")
    ResultSet get(String space, String itemId, String versionId, String revisionId);

    @Query("UPDATE version_elements SET conflict_element_ids=conflict_element_ids+? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    void addConflictElements(Set<String> elementIds, String space, String itemId, String
        versionId, String revisionId);

    @Query("UPDATE version_elements SET conflict_element_ids=conflict_element_ids-? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    void removeConflictElements(Set<String> elementIds, String space, String itemId,
                                String versionId, String revisionId);

    @Query("SELECT conflict_element_ids FROM version_elements " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    ResultSet getConflicted(String space, String itemId, String versionId, String revisionId);
  }

  private static final class StageElementsField {
    private static final String STAGE_ELEMENT_IDS = "stage_element_ids";
    private static final String CONFLICT_ELEMENT_IDS = "conflict_element_ids";
  }
}
