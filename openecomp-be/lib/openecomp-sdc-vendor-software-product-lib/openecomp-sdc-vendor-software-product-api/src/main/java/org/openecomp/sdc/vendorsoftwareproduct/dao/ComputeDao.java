package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ComputeDao extends VersionableDao, BaseDao<ComputeEntity> {

  Collection<ComputeEntity> listByVsp(String vspId, Version version);


  void updateQuestionnaireData(String vspId, Version version, String componentId, String computeId,
                               String questionnaireData);
  void deleteAll(String vspId, Version version);

  ComputeEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                 String computeId);
}
