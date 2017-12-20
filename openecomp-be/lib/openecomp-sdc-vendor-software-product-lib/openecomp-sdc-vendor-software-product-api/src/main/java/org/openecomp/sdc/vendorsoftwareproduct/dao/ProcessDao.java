package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

/**
 * @author Avrahamg.
 * @since March 23, 2017
 */
public interface ProcessDao extends VersionableDao, BaseDao<ProcessEntity> {
  void deleteAll(ProcessEntity entity);

  void deleteVspAll(String vspId, Version version);

  ProcessEntity getArtifact(ProcessEntity entity);

  void uploadArtifact(ProcessEntity entity);

  void deleteArtifact(ProcessEntity entity);
}
