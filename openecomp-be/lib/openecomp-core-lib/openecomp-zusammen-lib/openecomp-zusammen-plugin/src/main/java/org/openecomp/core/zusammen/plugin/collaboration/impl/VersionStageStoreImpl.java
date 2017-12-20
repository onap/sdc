package org.openecomp.core.zusammen.plugin.collaboration.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.plugin.collaboration.VersionStageStore;
import org.openecomp.core.zusammen.plugin.dao.VersionStageRepository;
import org.openecomp.core.zusammen.plugin.dao.VersionStageRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionContext;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Optional;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getPrivateSpaceName;

public class VersionStageStoreImpl implements VersionStageStore {
  @Override
  public Optional<StageEntity<VersionEntity>> get(SessionContext context, Id itemId,
                                                  VersionEntity versionEntity) {
    return getVersionStageRepository(context)
        .get(context, new VersionContext(getPrivateSpaceName(context), itemId), versionEntity);
  }

  @Override
  public void create(SessionContext context, Id itemId, StageEntity<VersionEntity> versionStage) {
    getVersionStageRepository(context)
        .create(context, new VersionContext(getPrivateSpaceName(context), itemId), versionStage);
  }

  @Override
  public void delete(SessionContext context, Id itemId, VersionEntity version) {
    getVersionStageRepository(context)
        .delete(context, new VersionContext(getPrivateSpaceName(context), itemId), version);
  }

  protected VersionStageRepository getVersionStageRepository(SessionContext context) {
    return VersionStageRepositoryFactory.getInstance().createInterface(context);
  }
}
