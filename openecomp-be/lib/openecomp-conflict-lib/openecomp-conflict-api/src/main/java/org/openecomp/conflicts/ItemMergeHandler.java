package org.openecomp.conflicts;

import org.openecomp.conflicts.types.Conflict;
import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Optional;

public interface ItemMergeHandler {

  boolean isConflicted(String itemId, Version version);

  void finalizeMerge(String itemId, Version version);

  void postListConflicts(String itemId, Version version, ItemVersionConflict conflicts);

  Optional<Conflict> getConflict(String itemId, Version version, String conflictId);

  void postGetConflict(String itemId, Version version, Conflict conflict);

  void preResolveConflict(String itemId, Version version, String conflictId,
                          ConflictResolution resolution);

  boolean resolveConflict(String itemId, Version version, String conflictId,
                          ConflictResolution resolution);
}
