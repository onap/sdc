package org.openecomp.sdc.vendorsoftwareproduct.dao;


import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ImageDao extends VersionableDao, BaseDao<ImageEntity> {

  void updateQuestionnaireData(String vspId, Version version, String componentId, String imageId,
                               String questionnaireData);


  void deleteByVspId(String vspId, Version version);

  Collection<ImageEntity> listByVsp(String vspId, Version version);

  ImageEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                     String imageId);
}
