package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public interface VersionPublicStore {

  Optional<VersionEntity> get(SessionContext context, Id itemId, Id versionId);

  Optional<SynchronizationStateEntity> getSynchronizationState(SessionContext context,
                                                               Id itemId, Id versionId);

  void create(SessionContext context, Id itemId, VersionEntity version, Id revisionId,
              Map<Id, Id> versionElementIds, Date publishTime, String message);

  void update(SessionContext context, Id itemId, VersionEntity version, Id revisionId,
              Map<Id, Id> versionElementIds, Date publishTime, String message);

  boolean checkHealth(SessionContext context);

  ItemVersionRevisions listItemVersionRevisions(SessionContext context, Id itemId, Id versionId);
}
