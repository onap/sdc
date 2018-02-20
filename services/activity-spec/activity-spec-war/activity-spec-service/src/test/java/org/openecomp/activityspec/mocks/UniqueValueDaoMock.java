package org.openecomp.activityspec.mocks;

import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.types.UniqueValueEntity;

import java.util.Collection;

public class UniqueValueDaoMock implements UniqueValueDao {
  @Override
  public Collection<UniqueValueEntity> list(UniqueValueEntity entity) {
    return null;
  }

  @Override
  public void create(UniqueValueEntity entity) {

  }

  @Override
  public void update(UniqueValueEntity entity) {

  }

  @Override
  public UniqueValueEntity get(UniqueValueEntity entity) {
    return null;
  }

  @Override
  public void delete(UniqueValueEntity entity) {

  }
}
