package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Collection;
import java.util.Optional;

public class CommitStagingService {

  private VersionPrivateStore versionPrivateStore;
  private VersionStageStore versionStageStore;
  private ElementPrivateStore elementPrivateStore;
  private ElementStageStore elementStageStore;

  public CommitStagingService(VersionPrivateStore versionPrivateStore,
                              VersionStageStore versionStageStore,
                              ElementPrivateStore elementPrivateStore,
                              ElementStageStore elementStageStore) {
    this.versionPrivateStore = versionPrivateStore;
    this.versionStageStore = versionStageStore;
    this.elementPrivateStore = elementPrivateStore;
    this.elementStageStore = elementStageStore;
  }

  public void commitStaging(SessionContext context, Id itemId, Id versionId) {
    Optional<StageEntity<VersionEntity>> versionStage =
        versionStageStore.get(context, itemId, new VersionEntity(versionId));

    final ElementContext elementContext = new ElementContext(itemId, versionId, Id.ZERO);
    Collection<ElementEntity> stagedElementIds = elementStageStore.listIds(context, elementContext);

    if ((!versionStage.isPresent() && stagedElementIds.isEmpty()) ||
        elementStageStore.hasConflicts(context, elementContext)) {
      return;
    }

    versionStage.ifPresent(verStage -> commitVersionStage(context, itemId, verStage));
    commitElementsStage(context, elementContext, stagedElementIds);
  }

  private void commitVersionStage(SessionContext context, Id itemId,
                                  StageEntity<VersionEntity> versionStage) {
    switch (versionStage.getAction()) {
      case CREATE:
        versionPrivateStore.commitStagedCreate(context, itemId, versionStage.getEntity(),
            versionStage.getPublishTime());
        break;
      case UPDATE:
        versionPrivateStore.commitStagedUpdate(context, itemId, versionStage.getEntity(),
            versionStage.getPublishTime());
        break;
      case IGNORE:
        versionPrivateStore.commitStagedIgnore(context, itemId, versionStage.getEntity(),
            versionStage.getPublishTime());
        break;
      default:
        throw new UnsupportedOperationException(
            "Version change other then Create/Update/Ignore is not supported");
    }

    versionStageStore.delete(context, itemId, versionStage.getEntity());
  }

  private void commitElementsStage(SessionContext context, ElementContext elementContext,
                                   Collection<ElementEntity> stagedElementIds) {
    for (ElementEntity stagedElementId : stagedElementIds) {
      StageEntity<ElementEntity> stagedElement =
          elementStageStore.get(context, elementContext, stagedElementId)
              .orElseThrow(
                  () -> new IllegalStateException("Element id returned by list must exist"));
      switch (stagedElement.getAction()) {
        case CREATE:
          elementPrivateStore.commitStagedCreate(context, elementContext, stagedElement.getEntity(),
              stagedElement.getPublishTime());
          break;
        case UPDATE:
          elementPrivateStore.commitStagedUpdate(context, elementContext, stagedElement.getEntity(),
              stagedElement.getPublishTime());
          break;
        case DELETE:
          elementPrivateStore
              .commitStagedDelete(context, elementContext, stagedElement.getEntity());
          break;
        case IGNORE:
          elementPrivateStore.commitStagedIgnore(context, elementContext, stagedElement.getEntity(),
              stagedElement.getPublishTime());
          break;
        default:
          throw new UnsupportedOperationException(
              "Element change other then Create/Update/Delete/Ignore is not supported");
      }
      elementStageStore.delete(context, elementContext, stagedElement.getEntity());
    }
  }
}
