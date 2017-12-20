package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.zusammen.plugin.dao.VersionSynchronizationStateRepository;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionContext;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VersionSynchronizationStateRepositoryImpl
    implements VersionSynchronizationStateRepository {

  @Override
  public void create(SessionContext context, VersionContext entityContext,
                     SynchronizationStateEntity syncStateEntity) {
    updatePublishTime(context, entityContext, syncStateEntity);
  }

  @Override
  public void updatePublishTime(SessionContext context, VersionContext entityContext,
                                SynchronizationStateEntity syncStateEntity) {
    getAccessor(context)
        .updatePublishTime(syncStateEntity.getPublishTime(), entityContext.getSpace(),
            entityContext.getItemId().toString(), syncStateEntity.getId().toString(),
            syncStateEntity.getRevisionId().getValue());
  }

  @Override
  public List<SynchronizationStateEntity> list(SessionContext context, VersionContext
      entityContext, VersionEntity versionEntity) {

    List<Row> rows = getAccessor(context).list(entityContext.getSpace(), entityContext.getItemId().toString
        (),versionEntity.getId().toString()).all();
    return rows == null ? new ArrayList<>():
        rows.stream().map(VersionSynchronizationStateRepositoryImpl::getSynchronizationStateEntity).collect(Collectors.toList());
  }



  /*@Override
  public List<SynchronizationStateEntity> listRevisions(SessionContext context,
                                                        VersionContext entityContext,
                                                        SynchronizationStateEntity syncStateEntity) {
    List<Row> rows = getAccessor(context).list(entityContext.getSpace(), entityContext.getItemId()
        .toString(), syncStateEntity.getId().toString()).all();
    return rows == null ? new ArrayList<>() :rows.stream()
        .map(VersionSynchronizationStateRepositoryImpl::getSynchronizationStateEntity)
        .collect(Collectors.toList());




    //forEach(row -> getSynchronizationStateEntity(syncStateEntity.getId(), row));


  }*/


  @Override
  public void delete(SessionContext context, VersionContext entityContext,
                     SynchronizationStateEntity syncStateEntity) {
    // done by version dao
  }

  @Override
  public Optional<SynchronizationStateEntity> get(SessionContext context,
                                                  VersionContext entityContext,
                                                  SynchronizationStateEntity syncStateEntity) {
    Row row =
        getAccessor(context).get(entityContext.getSpace(), entityContext.getItemId().toString(),
            syncStateEntity.getId().toString(), syncStateEntity.getRevisionId().getValue()).one();

    return row == null ? Optional.empty()
        : Optional.of(getSynchronizationStateEntity(syncStateEntity.getId(), row));
  }

  private SynchronizationStateEntity getSynchronizationStateEntity(Id entityId, Row row) {
    SynchronizationStateEntity syncStateEntity = new SynchronizationStateEntity(entityId,
        new Id(row.getString(REVISION_ID_FIELD)));
    syncStateEntity.setPublishTime(row.getDate(PUBLISH_TIME_FIELD));
    syncStateEntity.setDirty(!row.getSet(DIRTY_ELEMENT_FIELD, String.class).isEmpty());
    return syncStateEntity;
  }

  private static SynchronizationStateEntity getSynchronizationStateEntity(Row row) {
    Id entityId = new Id(row.getColumnDefinitions().contains("version_id") ? row.getString
        ("version_id") : row.getString("element_id"));
    SynchronizationStateEntity syncStateEntity = new SynchronizationStateEntity(entityId,
        new Id(row.getString(REVISION_ID_FIELD)));
    syncStateEntity.setPublishTime(row.getDate(PUBLISH_TIME_FIELD));
    syncStateEntity.setDirty(!row.getSet(DIRTY_ELEMENT_FIELD, String.class).isEmpty());
    syncStateEntity.setRevisionId(new Id(row.getString(REVISION_ID_FIELD)));
    syncStateEntity.setUser(row.getString(USER));
    syncStateEntity.setMessage(row.getString(MESSAGE));
    return syncStateEntity;
  }

  private VersionSyncStateAccessor getAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, VersionSyncStateAccessor.class);
  }

  @Accessor
  interface VersionSyncStateAccessor {
    @Query(
        "UPDATE version_elements SET publish_time=? WHERE space=? AND item_id=? AND version_id=? " +
            "AND revision_id=? ")
    void updatePublishTime(Date publishTime, String space, String itemId, String versionId, String
        revisionId);

    @Query("SELECT version_id,revision_id,publish_time, dirty_element_ids FROM version_elements " +
        "WHERE space=? AND item_id=? AND version_id=? AND revision_id=? ")
    ResultSet get(String space, String itemId, String versionId, String revisionId);

    @Query("SELECT version_id,revision_id,publish_time,user,message, dirty_element_ids FROM " +
        "version_elements " +
        "WHERE space=? AND item_id=? AND version_id=? ")
    ResultSet list(String space, String itemId, String versionId);

  }


  private static final String PUBLISH_TIME_FIELD = "publish_time";
  private static final String DIRTY_ELEMENT_FIELD = "dirty_element_ids";
  private static final String REVISION_ID_FIELD = "revision_id";
  private static final String USER = "user";
  private static final String MESSAGE = "message";
}
