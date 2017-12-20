package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Optional;

public interface VersionStageStore {

  Optional<StageEntity<VersionEntity>> get(SessionContext context, Id itemId,
                                           VersionEntity versionEntity);

  void create(SessionContext context, Id itemId, StageEntity<VersionEntity> versionStage);

  void delete(SessionContext context, Id itemId, VersionEntity version);
}
