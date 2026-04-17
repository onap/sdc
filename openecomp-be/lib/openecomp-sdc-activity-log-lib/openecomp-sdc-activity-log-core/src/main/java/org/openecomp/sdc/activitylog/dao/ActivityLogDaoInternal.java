package org.openecomp.sdc.activitylog.dao;

import java.util.List;

import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;

@Dao
public interface ActivityLogDaoInternal {
    @Query("SELECT * FROM activity_log WHERE item_id = :itemId AND version_id = :versionId")
    List<ActivityLogEntity> listByItemVersion(String itemId, String versionId);

    // void save(ActivityLogEntity entity);
}

