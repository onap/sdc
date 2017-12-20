package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;

public interface ComponentDependencyModelDao extends VersionableDao,
    BaseDao<ComponentDependencyModelEntity> {
}
