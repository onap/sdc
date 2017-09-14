package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface ComponentDependencyModelDao extends VersionableDao,
    BaseDao<ComponentDependencyModelEntity> {

  public void deleteAll(String vspId, Version version);
}
