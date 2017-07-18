package org.openecomp.sdc.vendorlicense.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;

public interface LimitDao extends VersionableDao, BaseDao<LimitEntity> {

  boolean isLimitPresent(LimitEntity limitEntity);
}
