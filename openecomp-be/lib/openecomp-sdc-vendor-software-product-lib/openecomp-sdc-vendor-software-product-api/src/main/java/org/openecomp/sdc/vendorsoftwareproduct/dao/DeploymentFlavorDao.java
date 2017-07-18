package org.openecomp.sdc.vendorsoftwareproduct.dao;


import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface DeploymentFlavorDao extends VersionableDao, BaseDao<DeploymentFlavorEntity> {
  void deleteAll(String vspId, Version version);
}

