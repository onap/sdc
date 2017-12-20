package org.openecomp.sdc.conflicts.impl;

import org.openecomp.conflicts.ItemMergeHandler;
import org.openecomp.conflicts.ItemMergeHandlerFactory;
import org.openecomp.conflicts.dao.ConflictsDao;
import org.openecomp.conflicts.types.Conflict;
import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.conflicts.types.Resolution;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.conflicts.ConflictsManager;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Optional;

public class ConflictsManagerImpl implements ConflictsManager {

  private static final String ELEMENT_CONFLICT_NOT_EXIST_ERR_ID = "ELEMENT_CONFLICT_NOT_EXIST";
  private static final String ELEMENT_CONFLICT_NOT_EXISTS_MSG =
      "Item Id %s, version Id %s, element conflict with Id %s does not exists.";

  private final ConflictsDao conflictsDao;

  public ConflictsManagerImpl(ConflictsDao conflictsDao) {
    this.conflictsDao = conflictsDao;
  }

  @Override
  public boolean isConflicted(String itemId, Version version) {
    Optional<ItemMergeHandler> itemMergeHandler =
        ItemMergeHandlerFactory.getInstance().createInterface(itemId);

    return conflictsDao.isConflicted(itemId, version) ||
        (itemMergeHandler.isPresent() &&
            itemMergeHandler.get().isConflicted(itemId, version));
  }

  @Override
  public ItemVersionConflict getConflict(String itemId, Version version) {
    ItemVersionConflict conflicts = conflictsDao.getConflict(itemId, version);

    ItemMergeHandlerFactory.getInstance().createInterface(itemId)
        .ifPresent(itemMergeHandler -> itemMergeHandler
            .postListConflicts(itemId, version, conflicts));

    return conflicts;
  }

  @Override
  public void finalizeMerge(String itemId, Version version) {
    ItemMergeHandlerFactory.getInstance().createInterface(itemId)
        .ifPresent(mergeHandler -> mergeHandler.finalizeMerge(itemId, version));
  }

  @Override
  public Conflict getConflict(String itemId, Version version, String conflictId) {
    Optional<ItemMergeHandler> itemMergeHandler =
        ItemMergeHandlerFactory.getInstance().createInterface(itemId);
    if (itemMergeHandler.isPresent()) {
      Optional<Conflict> conflict =
          itemMergeHandler.get().getConflict(itemId, version, conflictId);
      if (conflict.isPresent()) {
        return conflict.get();
      }
    }

    Conflict conflict = conflictsDao.getConflict(itemId, version, conflictId);

    itemMergeHandler.ifPresent(mergeHandler ->
        mergeHandler.postGetConflict(itemId, version, conflict));

    if (conflict == null) {
      throw getConflictNotExistException(itemId, version, conflictId);
    }
    return conflict;
  }

  @Override
  public void resolveConflict(String itemId, Version version, String conflictId,
                              ConflictResolution resolution) {
    if (Resolution.OTHER.equals(resolution.getResolution())) {
      throw new UnsupportedOperationException(
          "Resolution other than 'THEIRS' or 'YOURS' is not supported.");
    }

    Optional<ItemMergeHandler> itemMergeHandler =
        ItemMergeHandlerFactory.getInstance().createInterface(itemId);
    if (!itemMergeHandler.isPresent() ||
        !itemMergeHandler.get()
            .resolveConflict(itemId, version, conflictId, resolution)) {

      getConflict(itemId, version, conflictId); // validate that the conflict exist
      itemMergeHandler.ifPresent(mergeHandler ->
          mergeHandler.preResolveConflict(itemId, version, conflictId, resolution));

      conflictsDao.resolveConflict(itemId, version, conflictId, resolution);
    }
  }

  private CoreException getConflictNotExistException(String itemId, Version version,
                                                     String conflictId) {
    return new CoreException(new ErrorCode.ErrorCodeBuilder()
        .withCategory(ErrorCategory.APPLICATION)
        .withId(ELEMENT_CONFLICT_NOT_EXIST_ERR_ID)
        .withMessage(
            String.format(ELEMENT_CONFLICT_NOT_EXISTS_MSG, itemId, version.getId(), conflictId))
        .build());
  }

}
