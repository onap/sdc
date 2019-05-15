package org.openecomp.sdc.activitylog.impl;

import org.openecomp.sdc.activitylog.dao.ActivityLogDao;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActivityLogDaoStub implements ActivityLogDao {
    @Override
    public Collection<ActivityLogEntity> list(ActivityLogEntity entity) {
        List<ActivityLogEntity> list = new ArrayList<>();
        list.add(entity);
        return list;
    }

    @Override
    public void create(ActivityLogEntity entity) {
        //stub method
    }

    @Override
    public void update(ActivityLogEntity entity) {
        //stub method
    }

    @Override
    public ActivityLogEntity get(ActivityLogEntity entity) {
        return null;
    }

    @Override
    public void delete(ActivityLogEntity entity) {
        //stub method
    }
}
