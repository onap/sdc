package org.openecomp.conflicts.dao;

import org.openecomp.conflicts.types.Conflict;
import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface ConflictsDao {

  boolean isConflicted(String itemId, Version version);

  ItemVersionConflict getConflict(String itemId, Version version);

  Conflict getConflict(String itemId, Version version, String conflictId);

  void resolveConflict(String itemId, Version version, String conflictId,
                       ConflictResolution conflictResolution);
}
