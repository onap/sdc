package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.zusammen.plugin.dao.ElementSynchronizationStateRepository;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSynchronizationStateRepositoryImpl
    implements ElementSynchronizationStateRepository {

  @Override
  public Collection<SynchronizationStateEntity> list(SessionContext context,
                                                     ElementEntityContext elementContext) {
    List<Row> rows = getAccessor(context)
        .list(elementContext.getSpace(),
            elementContext.getItemId().toString(),
            elementContext.getVersionId().toString()).all();
    return rows == null ? new HashSet<>()
        : rows.stream().map(this::getSynchronizationStateEntity).collect(Collectors.toSet());

  }

  @Override
  public void create(SessionContext context, ElementEntityContext elementContext,
                     SynchronizationStateEntity elementSyncState) {
    update(context, elementContext.getSpace(),
        elementContext.getItemId(),
        elementContext.getVersionId(),
        elementContext.getRevisionId(),
        elementSyncState.getRevisionId(),
        elementSyncState.getId(),
        elementSyncState.getPublishTime(),
        elementSyncState.isDirty()

    );
  }

  @Override
  public void update(SessionContext context, ElementEntityContext elementContext,
                     SynchronizationStateEntity elementSyncState) {
    update(context, elementContext.getSpace(),
        elementContext.getItemId(),
        elementContext.getVersionId(),
        elementContext.getRevisionId(),
        elementSyncState.getRevisionId(),
        elementSyncState.getId(),
        elementSyncState.getPublishTime(),
        elementSyncState.isDirty()

    );
  }

  @Override
  public void markAsDirty(SessionContext context, ElementEntityContext elementContext,
                          SynchronizationStateEntity elementSyncState) {


      getAccessor(context).updateDirty(true,
          elementContext.getSpace(),
          elementContext.getItemId().toString(),
          elementContext.getVersionId().toString(),
          elementSyncState.getId().toString(),
          elementContext.getRevisionId().getValue());

      getVersionElementsAccessor(context).addDirtyElements(
          Collections.singleton(elementSyncState.getId().toString()), elementContext.getSpace(),
          elementContext.getItemId().toString(),
          elementContext.getVersionId().toString(),
          elementContext.getRevisionId().getValue());
  }

  @Override
  public void delete(SessionContext context, ElementEntityContext elementContext,
                     SynchronizationStateEntity elementSyncState) {
    getAccessor(context).delete(elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        elementSyncState.getId().toString(),
        elementContext.getRevisionId().getValue());

    getVersionElementsAccessor(context).removeDirtyElements(
        Collections.singleton(elementSyncState.getId().toString()), elementContext.getSpace(),
        elementContext.getItemId().toString(),
        elementContext.getVersionId().toString(),
        elementContext.getRevisionId().getValue());
  }

  @Override
  public Optional<SynchronizationStateEntity> get(SessionContext context,
                                                  ElementEntityContext elementContext,
                                                  SynchronizationStateEntity elementSyncState) {

    Row row = getAccessor(context)
        .get(elementContext.getSpace(),
            elementContext.getItemId().toString(),
            elementContext.getVersionId().toString(),
            elementSyncState.getId().toString(),
            elementSyncState.getRevisionId().getValue()).one();

    return row == null ? Optional.empty() : Optional.of(getSynchronizationStateEntity(row));
  }

  private void update(SessionContext context, String space, Id itemId, Id versionId, Id
      versionRevisionId,Id elementRevisionId,Id elementId, Date publishTime, boolean isDirty) {
    getAccessor(context).update(publishTime,
        isDirty,
        space,
        itemId.toString(),
        versionId.toString(),
        elementId.toString(),
        elementRevisionId.getValue());

    if (isDirty) {
      getVersionElementsAccessor(context).addDirtyElements(
          Collections.singleton(elementId.toString()), space,
          itemId.toString(),
          versionId.toString(),
          versionRevisionId.getValue());
    } else {
      getVersionElementsAccessor(context).removeDirtyElements(
          Collections.singleton(elementId.toString()), space,
          itemId.toString(),
          versionId.toString(),
          versionRevisionId.getValue());
    }
  }


  private SynchronizationStateEntity getSynchronizationStateEntity(Row row) {
    SynchronizationStateEntity entity =
        new SynchronizationStateEntity(new Id(row.getString(SynchronizationStateField.ID)),
            new Id(row.getString(SynchronizationStateField.REVISION_ID)),
            row.getDate(SynchronizationStateField.PUBLISH_TIME),
            row.getBool(SynchronizationStateField.DIRTY));
    entity.setRevisionId(new Id(row.getString(SynchronizationStateField.REVISION_ID)));

    return entity;

  }

  private ElementSynchronizationStateAccessor getAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, ElementSynchronizationStateAccessor.class);
  }

  private VersionElementsAccessor getVersionElementsAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, VersionElementsAccessor.class);
  }

  @Accessor
  interface ElementSynchronizationStateAccessor {
    @Query("UPDATE element_synchronization_state SET publish_time=?, dirty=? " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id = ? ")
    void update(Date publishTime, boolean dirty, String space, String itemId, String versionId,
                String elementId, String revisionId);

    @Query("UPDATE element_synchronization_state SET dirty=? " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id = ? ")
    void updateDirty(boolean dirty, String space, String itemId, String versionId,
                     String elementId, String revisionId);

    @Query("DELETE FROM element_synchronization_state " +
        "WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id = ? ")
    void delete(String space, String itemId, String versionId, String elementId, String revisionId);

    @Query("SELECT element_id,revision_id, publish_time, dirty FROM element_synchronization_state" +
        " WHERE space=? AND item_id=? AND version_id=?")
    ResultSet list(String space, String itemId, String versionId);

    @Query("SELECT element_id,revision_id, publish_time, dirty FROM element_synchronization_state" +
        " WHERE space=? AND item_id=? AND version_id=? AND element_id=? AND revision_id = ?  ")
    ResultSet get(String space, String itemId, String versionId, String elementId, String
        revisionId);
  }

  private static final class SynchronizationStateField {
    private static final String ID = "element_id";
    private static final String PUBLISH_TIME = "publish_time";
    private static final String DIRTY = "dirty";
    private static final String REVISION_ID = "revision_id";
  }

  @Accessor
  interface VersionElementsAccessor {

    @Query("UPDATE version_elements SET dirty_element_ids=dirty_element_ids+? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=?")
    void addDirtyElements(Set<String> elementIds, String space, String itemId, String versionId,
                          String revisionId);

    @Query("UPDATE version_elements SET dirty_element_ids=dirty_element_ids-? " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    void removeDirtyElements(Set<String> elementIds, String space, String itemId, String
        versionId, String revisionId);
  }
}
