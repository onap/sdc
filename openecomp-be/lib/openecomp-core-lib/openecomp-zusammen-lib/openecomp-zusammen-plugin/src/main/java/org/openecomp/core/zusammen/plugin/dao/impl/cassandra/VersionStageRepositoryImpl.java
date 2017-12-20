package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.zusammen.plugin.dao.VersionStageRepository;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionContext;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Date;
import java.util.Optional;

public class VersionStageRepositoryImpl implements VersionStageRepository {

  @Override
  public Optional<StageEntity<VersionEntity>> get(SessionContext context,
                                                  VersionContext entityContext,
                                                  VersionEntity entity) {
    Row row = getAccessor(context)
        .get(entityContext.getSpace(), entityContext.getItemId().toString(),
            entity.getId().toString()).one();
    return row == null ? Optional.empty() : Optional.of(convertToVersionStage(entity, row));
  }

  @Override
  public void create(SessionContext context, VersionContext entityContext,
                     StageEntity<VersionEntity> stageEntity) {
    VersionEntity entity = stageEntity.getEntity();
    getAccessor(context).create(entityContext.getSpace(),
        entityContext.getItemId().toString(),
        entity.getId().toString(),
        entity.getBaseId() == null ? null : entity.getBaseId().toString(),
        entity.getCreationTime() == null ? null : entity.getCreationTime(),
        entity.getModificationTime() == null ? null : entity.getModificationTime(),
        stageEntity.getPublishTime(),
        stageEntity.getAction());
  }

  @Override
  public void delete(SessionContext context, VersionContext entityContext, VersionEntity entity) {
    getAccessor(context).delete(entityContext.getSpace(), entityContext.getItemId().toString(),
        entity.getId().toString());
  }

  private StageEntity<VersionEntity> convertToVersionStage(VersionEntity version, Row row) {
    StageEntity<VersionEntity> versionStage =
        new StageEntity<>(VersionDaoImpl.enrichVersionEntity(version, row),
            row.getDate(VersionStageField.PUBLISH_TIME));
    versionStage.setAction(Action.valueOf(row.getString(VersionStageField.ACTION)));
    return versionStage;
  }

  private VersionStageAccessor getAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, VersionStageAccessor.class);
  }

  @Accessor
  interface VersionStageAccessor {

    @Query("INSERT INTO version_stage (space, item_id, version_id, base_version_id, " +
        "creation_time, modification_time, publish_time, action) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
    void create(String space, String itemId, String versionId, String baseVersionId,
                Date creationTime, Date modificationTime, Date publishTime, Action action);

    @Query("DELETE FROM version_stage WHERE space=? AND item_id=? AND version_id=?")
    void delete(String space, String itemId, String versionId);

    @Query("SELECT base_version_id, creation_time, modification_time, publish_time, action " +
        "FROM  version_stage WHERE space=? AND item_id=? AND version_id=?")
    ResultSet get(String space, String itemId, String versionId);
  }

  private static final class VersionStageField {
    private static final String PUBLISH_TIME = "publish_time";
    private static final String ACTION = "action";
  }
}
