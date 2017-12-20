package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeConflict;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID;

public class SyncService {
  private static final String PULL_NON_EXISTING_VERSION =
      "Item Id %s, version Id %s: Non existing version cannot be synced.";
  private static final String PUBLIC_SYNC_STATE_EXISTS_WITHOUT_ELEMENT =
      "Item Id %s, version Id %s: Sync state of element with Id %s " +
          "exists in public space while the element does not";
  private static final String PRIVATE_UNPUBLISHED_SYNC_STATE_EXISTS_WITHOUT_ELEMENT =
      "Item Id %s, version Id %s: Sync state of unpublished element with Id %s " +
          "exists in private space while the element does not";

  private VersionPublicStore versionPublicStore;
  private VersionPrivateStore versionPrivateStore;
  private VersionStageStore versionStageStore;
  private ElementPublicStore elementPublicStore;
  private ElementPrivateStore elementPrivateStore;
  private ElementStageStore elementStageStore;

  public SyncService(VersionPublicStore versionPublicStore,
                     VersionPrivateStore versionPrivateStore,
                     VersionStageStore versionStageStore,
                     ElementPublicStore elementPublicStore,
                     ElementPrivateStore elementPrivateStore,
                     ElementStageStore elementStageStore) {
    this.versionPublicStore = versionPublicStore;
    this.versionPrivateStore = versionPrivateStore;
    this.versionStageStore = versionStageStore;
    this.elementPublicStore = elementPublicStore;
    this.elementPrivateStore = elementPrivateStore;
    this.elementStageStore = elementStageStore;
  }

  public CollaborationMergeResult sync(SessionContext context, Id itemId, Id versionId,
                                       boolean force) {
    SynchronizationStateEntity publicVersionSyncState =
        versionPublicStore.getSynchronizationState(context, itemId, versionId)
            .orElseThrow(() -> new IllegalStateException(
                String.format(PULL_NON_EXISTING_VERSION, itemId.toString(), versionId.toString())));

    Date publishTime = publicVersionSyncState.getPublishTime();

    Optional<SynchronizationStateEntity> privateVersionSyncState =
        versionPrivateStore.getSynchronizationState(context, itemId, versionId);

    if (force || !privateVersionSyncState.isPresent() ||
        !publishTime.equals(privateVersionSyncState.get().getPublishTime())) {
      ElementContext elementContext =
          new ElementContext(itemId, versionId, publicVersionSyncState.getRevisionId());

      Collection<SynchronizationStateEntity> publicSyncStates =
          elementPublicStore.listSynchronizationStates(context, elementContext);
      Collection<SynchronizationStateEntity> privateSyncStates =
          elementPrivateStore.listSynchronizationStates(context, elementContext);
      Map<Id, SynchronizationStateEntity> publicSyncStateById = toMapById(publicSyncStates);

      Set<Id> syncedElements = new HashSet<>();
      if (force) {
        List<SynchronizationStateEntity> dirtyPrivateSyncStates = privateSyncStates.stream()
            .filter(SynchronizationStateEntity::isDirty)
            .collect(Collectors.toList());

        forceSyncDirtyElements(context, elementContext, dirtyPrivateSyncStates, publicSyncStateById,
            syncedElements);
      }

      if (!privateVersionSyncState.isPresent() ||
          !publishTime.equals(privateVersionSyncState.get().getPublishTime())) {
        syncVersion(context, itemId, versionId, publishTime, privateVersionSyncState.isPresent());
        syncElements(context, elementContext,
            privateVersionSyncState.map(SynchronizationStateEntity::getPublishTime).orElse(null),
            publicSyncStates, privateSyncStates, publicSyncStateById, syncedElements);
      }
    }

    return createResult();
  }

  private CollaborationMergeResult createResult() {
    CollaborationMergeResult result = new CollaborationMergeResult();
    result.setChange(new CollaborationMergeChange());
    result.setConflict(new CollaborationMergeConflict());
    return result;
  }

  private void syncVersion(SessionContext context, Id itemId, Id versionId, Date publishTime,
                           boolean versionExistOnPrivate) {
    if (versionExistOnPrivate) {
      stageVersion(context, itemId, new VersionEntity(versionId), Action.UPDATE, publishTime);
    } else {
      stageVersion(context, itemId, versionPublicStore.get(context, itemId, versionId)
              .orElseThrow(() -> new IllegalArgumentException(String
                  .format(PULL_NON_EXISTING_VERSION, itemId.toString(), versionId.toString()))),
          Action.CREATE, publishTime);
    }
  }

  private void syncElements(SessionContext context, ElementContext elementContext,
                            Date previousSyncedPublishTime,
                            Collection<SynchronizationStateEntity> publicSyncStates,
                            Collection<SynchronizationStateEntity> privateSyncStates,
                            Map<Id, SynchronizationStateEntity> publicSyncStateById,
                            Set<Id> syncedElements) {
    Map<Id, SynchronizationStateEntity> privateSyncStateById = toMapById(privateSyncStates);

    Collection<SynchronizationStateEntity> updatedPublicSyncStates =
        previousSyncedPublishTime == null
            ? publicSyncStates
            : publicSyncStates.stream()
                .filter(syncState -> syncState.getPublishTime().after(previousSyncedPublishTime))
                .collect(Collectors.toList());

    syncPublicUpdatedElements(context, elementContext, updatedPublicSyncStates,
        publicSyncStateById, privateSyncStateById, syncedElements);

    List<SynchronizationStateEntity> onlyOnPrivatePublishedSyncStates =
        privateSyncStates.stream()
            .filter(syncState -> !publicSyncStateById.containsKey(syncState.getId()) &&
                syncState.getPublishTime() != null)
            .collect(Collectors.toList());

    syncPublicDeletedElements(context, elementContext, onlyOnPrivatePublishedSyncStates,
        publicSyncStateById, privateSyncStateById, syncedElements);
  }

  private void syncPublicUpdatedElements(SessionContext context, ElementContext elementContext,
                                         Collection<SynchronizationStateEntity> updatedPublicSyncStates,
                                         Map<Id, SynchronizationStateEntity> publicSyncStateById,
                                         Map<Id, SynchronizationStateEntity> privateSyncStateById,
                                         Set<Id> syncedElements) {
    for (SynchronizationStateEntity publicSyncState : updatedPublicSyncStates) {
      if (syncedElements.contains(publicSyncState.getId())) {
        continue;
      }

      ElementEntity publicElement =
          elementPublicStore.get(context, elementContext, publicSyncState.getId()).orElseThrow(
              () -> new IllegalStateException(String
                  .format(PUBLIC_SYNC_STATE_EXISTS_WITHOUT_ELEMENT,
                      elementContext.getItemId().getValue(),
                      elementContext.getVersionId().getValue(),
                      publicSyncState.getId().getValue())));

      SynchronizationStateEntity privateSyncState =
          privateSyncStateById.get(publicSyncState.getId());

      if (privateSyncState != null) {
        if (!privateSyncState.isDirty()) {
          // not changed on private
          stageElement(context, elementContext, publicElement,
              publicSyncState.getPublishTime(),
              Action.UPDATE, false, null);
          syncedElements.add(publicSyncState.getId());
        } else {
          Optional<ElementEntity> privateElement =
              elementPrivateStore.get(context, elementContext, publicSyncState.getId());

          if (privateElement.isPresent()) {
            // updated on private - conflict if it has different hash
            stageElement(context, elementContext, publicElement,
                publicSyncState.getPublishTime(), Action.UPDATE,
                !publicElement.getElementHash().equals(privateElement.get().getElementHash()),
                null);

            syncedElements.add(publicSyncState.getId());
          } else {
            // deleted on private - conflict tree
            Set<Id> changeTreeElementIds =
                stagePublicElementTree(context, elementContext, publicElement, publicSyncStateById,
                    (treeElementIds) -> true);
            syncedElements.addAll(changeTreeElementIds);
          }
        }
      } else {
        // not existing on private - new creation on public
        Set<Id> changeTreeElementIds =
            stagePublicElementTree(context, elementContext, publicElement, publicSyncStateById,
                (treeElementIds) -> containsDirty(treeElementIds, privateSyncStateById));
        syncedElements.addAll(changeTreeElementIds);
      }
    }
  }

  private void syncPublicDeletedElements(
      SessionContext context, ElementContext elementContext,
      Collection<SynchronizationStateEntity> onlyOnPrivatePublishedSyncStates,
      Map<Id, SynchronizationStateEntity> publicSyncStateById,
      Map<Id, SynchronizationStateEntity> privateSyncStateById,
      Set<Id> syncedElements) {
    for (SynchronizationStateEntity privateSyncState : onlyOnPrivatePublishedSyncStates) {
      if (syncedElements.contains(privateSyncState.getId())) {
        continue;
      }

      Optional<ElementEntity> privateElement =
          elementPrivateStore.get(context, elementContext, privateSyncState.getId());

      if (!privateElement.isPresent()) {
        // deleted on private as well
        stageElement(context, elementContext, new ElementEntity(privateSyncState.getId()),
            null, Action.DELETE, false, null);
        syncedElements.add(privateSyncState.getId());
      } else {
        Set<Id> changeTreeElementIds =
            stageElementTree(context, elementContext, privateElement.get(),
                elementPrivateStore, publicSyncStateById::containsKey,
                (treeElementIds) -> containsDirty(treeElementIds, privateSyncStateById),
                (elementId) -> null, Action.DELETE);
        syncedElements.addAll(changeTreeElementIds);
      }
    }
  }

  private void forceSyncDirtyElements(SessionContext context, ElementContext elementContext,
                                      List<SynchronizationStateEntity> dirtyPrivateSyncStates,
                                      Map<Id, SynchronizationStateEntity> publicSyncStateById,
                                      Set<Id> syncedElements) {
    for (SynchronizationStateEntity privateSyncState : dirtyPrivateSyncStates) {
      Optional<ElementEntity> privateElement =
          elementPrivateStore.get(context, elementContext, privateSyncState.getId());
      if (privateSyncState.getPublishTime() == null) {
        stageElement(context, elementContext,
            privateElement.orElseThrow(() -> new IllegalStateException(
                String.format(PRIVATE_UNPUBLISHED_SYNC_STATE_EXISTS_WITHOUT_ELEMENT,
                    elementContext.getItemId().getValue(),
                    elementContext.getVersionId().getValue(),
                    privateSyncState.getId().getValue()))),
            null, Action.DELETE, false, null);
      } else {
        SynchronizationStateEntity publicSyncState =
            publicSyncStateById.get(privateSyncState.getId());
        if (publicSyncState != null) {
          ElementEntity publicElement =
              elementPublicStore.get(context, elementContext, privateSyncState.getId()).orElseThrow(
                  () -> new IllegalStateException(String
                      .format(PUBLIC_SYNC_STATE_EXISTS_WITHOUT_ELEMENT,
                          elementContext.getItemId().getValue(),
                          elementContext.getVersionId().getValue(),
                          privateSyncState.getId().getValue())));

          stageElement(context, elementContext, publicElement, publicSyncState.getPublishTime(),
              privateElement.isPresent() ? Action.UPDATE : Action.CREATE, false, null);
        } else {
          stageElement(context, elementContext, privateElement.isPresent()
                  ? privateElement.get()
                  : new ElementEntity(privateSyncState.getId()),
              null, Action.DELETE, false, null);
        }
      }
      syncedElements.add(privateSyncState.getId());
    }
  }

  private Set<Id> stagePublicElementTree(SessionContext context,
                                         ElementContext elementContext,
                                         ElementEntity publicElement,
                                         Map<Id, SynchronizationStateEntity> publicSyncStateById,
                                         Predicate<Set<Id>> isElementTreeConflicted) {


    return stageElementTree(context, elementContext, publicElement,
        elementPublicStore,
        (elementId) -> elementPrivateStore.getDescriptor(context, elementContext, elementId)
            .isPresent(),
        isElementTreeConflicted,
        (elementId) -> publicSyncStateById.get(elementId).getPublishTime(),
        Action.CREATE);
  }

  private Set<Id> stageElementTree(SessionContext context, ElementContext elementContext,
                                   ElementEntity element,
                                   ElementStore elementStore,
                                   Predicate<Id> isElementExist,
                                   Predicate<Set<Id>> isElementTreeConflicted,
                                   Function<Id, Date> stagePublishTimeGetter,
                                   Action stageAction) {
    ElementEntity elementTreeRoot = findRootElementOfChange(context, elementContext,
        elementStore, isElementExist, element);

    Set<Id> elementTreeIds = new HashSet<>();
    elementTreeIds.add(elementTreeRoot.getId());

    Set<Id> subElementIds = stageElementSubs(context, elementContext, elementStore, elementTreeRoot,
        stagePublishTimeGetter, stageAction);
    elementTreeIds.addAll(subElementIds);

    boolean conflicted = isElementTreeConflicted.test(elementTreeIds);
    stageElement(context, elementContext, elementTreeRoot,
        stagePublishTimeGetter.apply(elementTreeRoot.getId()), stageAction, conflicted,
        conflicted ? subElementIds : null);
    return elementTreeIds;
  }

  private ElementEntity findRootElementOfChange(SessionContext context,
                                                ElementContext elementContext,
                                                ElementStore elementStore,
                                                Predicate<Id> isElementExistOnOppositeStore,
                                                ElementEntity element) {
    return element.getId().equals(ROOT_ELEMENTS_PARENT_ID) ||
        isElementExistOnOppositeStore.test(element.getParentId())
        ? element
        : findRootElementOfChange(context, elementContext, elementStore,
            isElementExistOnOppositeStore,
            elementStore.get(context, elementContext, element.getParentId())
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Element %s exists while its parent element %s does not",
                        element.getId(), element.getParentId()))));
  }

  private boolean containsDirty(Set<Id> elementIds,
                                Map<Id, SynchronizationStateEntity> syncStateById) {
    return elementIds.stream().anyMatch(elementId -> {
      SynchronizationStateEntity privateSyncState = syncStateById.get(elementId);
      return privateSyncState != null && privateSyncState.isDirty();
    });
  }

  private Set<Id> stageElementSubs(SessionContext context, ElementContext elementContext,
                                   ElementStore elementStore, ElementEntity parentElement,
                                   Function<Id, Date> stagePublishTimeGetter, Action stageAction) {
    Set<Id> elementTreeIds = new HashSet<>();
    for (Id elementId : parentElement.getSubElementIds()) {
      ElementEntity element = elementStore.get(context, elementContext, elementId).get();

      stageElement(context, elementContext, element, stagePublishTimeGetter.apply(elementId),
          stageAction, false, null);

      elementTreeIds.add(elementId);
      elementTreeIds.addAll(
          stageElementSubs(context, elementContext, elementStore, element, stagePublishTimeGetter,
              stageAction));
    }
    return elementTreeIds;
  }

  private void stageElement(SessionContext context, ElementContext elementContext,
                            ElementEntity element, Date publishTime, Action action,
                            boolean conflicted, Set<Id> conflictDependents) {
    StageEntity<ElementEntity> elementStage =
        new StageEntity<>(element, publishTime, action, conflicted);
    if (conflictDependents != null) {
      elementStage.setConflictDependents(
          conflictDependents.stream().map(ElementEntity::new).collect(Collectors.toSet()));
    }
    elementStageStore.create(context, elementContext, elementStage);
  }

  private void stageVersion(SessionContext context, Id itemId, VersionEntity stageVersion,
                            Action stageAction, Date publishTime) {
    versionStageStore
        .create(context, itemId, new StageEntity<>(stageVersion, publishTime, stageAction, false));
  }

  private Map<Id, SynchronizationStateEntity> toMapById(
      Collection<SynchronizationStateEntity> syncStates) {
    return syncStates.stream()
        .collect(Collectors.toMap(SynchronizationStateEntity::getId, Function.identity()));
  }
}
