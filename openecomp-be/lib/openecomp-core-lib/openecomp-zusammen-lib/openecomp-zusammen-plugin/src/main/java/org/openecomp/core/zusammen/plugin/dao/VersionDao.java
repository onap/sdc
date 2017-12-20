package org.openecomp.core.zusammen.plugin.dao;


import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public interface VersionDao {
  Collection<VersionEntity> list(SessionContext context, String space, Id itemId);

  Optional<VersionEntity> get(SessionContext context, String space, Id itemId, Id versionId);

  void create(SessionContext context, String space, Id itemId, VersionEntity version);

  void updateModificationTime(SessionContext context, String space, Id itemId, Id versionId, Date modificationTime);

  void delete(SessionContext context, String space, Id itemId, Id versionId);

  boolean checkHealth(SessionContext context);

  void createVersionElements(SessionContext context, String publicSpace, Id itemId, Id versionId,
                             Id revisionId, Map<Id, Id> versionElementIds, Date publishTime,
                             String message);
}
