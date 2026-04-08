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

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.Collection;


import org.openecomp.core.dao.impl.CassandraBaseDao;

import org.openecomp.sdc.activitylog.dao.ActivityLogDao;
import org.openecomp.sdc.activitylog.dao.ActivityLogDaoInternal;
import org.openecomp.sdc.activitylog.dao.ActivityLogMapper;
import org.openecomp.sdc.activitylog.dao.ActivityLogMapperBuilder;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;


public class ActivityLogDaoCassandraImpl extends CassandraBaseDao<ActivityLogEntity> implements ActivityLogDao {

   private final ActivityLogDaoInternal dao;

    public ActivityLogDaoCassandraImpl(CqlSession session) {
        super(session);
        ActivityLogMapper mapper = new ActivityLogMapperBuilder(session).build();
        this.dao = mapper.activityLogDao(session.getKeyspace().get().asInternal());

    }


    @Override
    protected Object[] getKeys(ActivityLogEntity entity) {
        return new Object[]{entity.getItemId(), entity.getVersionId(), entity.getId()};
    }

    @Override
    public Collection<ActivityLogEntity> list(ActivityLogEntity entity) {
        return dao.listByItemVersion(entity.getItemId(), entity.getVersionId());
    }
    

    @Override
    protected String getTableName() {
        return "activity_log";
    }


    @Override
    protected String[] getColumns(ActivityLogEntity entity) {
        return new String[] {
        "item_id",
        "version_id",
        "activity_id",
        "type",
        "user",
        "timestamp",
        "success",
        "message",
        "comment"
    };
    }


    @Override
    protected Object[] getValues(ActivityLogEntity entity) {
        return new Object[] {
        entity.getItemId(),
        entity.getVersionId(),
        entity.getId(),
        entity.getType() == null ? null : entity.getType().name(),
        entity.getUser(),
        entity.getTimestamp(),
        entity.isSuccess(),
        entity.getMessage(),
        entity.getComment()
    };
    }
}
