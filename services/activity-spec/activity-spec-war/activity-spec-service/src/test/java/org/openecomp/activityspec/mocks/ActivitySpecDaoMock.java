package org.openecomp.activityspec.mocks;

import org.openecomp.activityspec.be.dao.ActivitySpecDao;
import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;

public class ActivitySpecDaoMock implements ActivitySpecDao {
  public ActivitySpecEntity activitySpec;

  @Override
  public void create(ActivitySpecEntity activitySpecEntity) {
    activitySpec = activitySpecEntity;
  }

  @Override
  public ActivitySpecEntity get(ActivitySpecEntity activitySpecEntity) {
    return activitySpec;
  }

  @Override
  public void update(ActivitySpecEntity activitySpecEntity) {
    activitySpec = activitySpecEntity;
  }
}
