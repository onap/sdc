/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.activitylog.dao.impl;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.activitylog.dao.ActivityLogDao;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;

import java.util.Collection;
import java.util.Date;

public class ActivityLogDaoCassandraImpl extends CassandraBaseDao<ActivityLogEntity> implements ActivityLogDao{
    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final Mapper<ActivityLogEntity> mapper =
            noSqlDb.getMappingManager().mapper(ActivityLogEntity.class);
    private static final ActivityLogAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(ActivityLogAccessor.class);

    @Override
    protected Mapper<ActivityLogEntity> getMapper() {
        return mapper;
    }

    @Override
    protected Object[] getKeys(ActivityLogEntity entity) {
        return new Object[0];
    }

    @Override
    public Collection<ActivityLogEntity> list(ActivityLogEntity entity) {
        return accessor.list().all();
    }

    @Override
    public void create(ActivityLogEntity activityLogEntity) {
        accessor.create(activityLogEntity.getItemId(), activityLogEntity.getVersionId(),activityLogEntity.getId(),
                activityLogEntity.getType(),activityLogEntity.getUser(), activityLogEntity.getTimestamp(), activityLogEntity.isSuccess(),
                activityLogEntity.getMessage(), activityLogEntity.getComment());
    }

    @Override
    public Collection<ActivityLogEntity> getActivityLogListForItem(String itemId, String versionId) {
        return accessor.getForItem(itemId, versionId).all();
    }


    @Accessor
    interface ActivityLogAccessor {
        @Query("select item_id, version_id, activity_id, type, user, timestamp, success, message, comment"
                        + " from activity_log")
        Result<ActivityLogEntity> list();

        @Query("select item_id, version_id, activity_id, type, user, timestamp, success, message, comment"
                        + " from activity_log where item_id=? and version_id=?")
        Result<ActivityLogEntity> getForItem(String itemId, String versionId);

        @Query("insert into activity_log " +
                " (item_id, version_id, activity_id, type, user, timestamp, success, message, comment)" +
                " values (?,?,?,?,?,?,?,?,?)")
        Result<ActivityLogEntity> create(String itemId, String versionId, String id, String type,
                                         String user, Date timestamp, boolean success,
                                         String message, String comment);
    }
}
