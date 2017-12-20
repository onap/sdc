package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.response.ReturnCode;
import com.amdocs.zusammen.datatypes.response.ZusammenException;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.amdocs.zusammen.datatypes.response.Module.ZCSP;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToElementChange;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToVersionChange;
import static org.openecomp.core.zusammen.plugin.collaboration.ErrorCode.NO_CHANGES_TO_PUBLISH;

public class PublishService {
  // TODO: 6/29/2017 throw ZusammenException with ReturnCode when needed.
  private static final String PUSH_NON_EXISTING_VERSION =
      "Item Id %s, version Id %s: Non existing version cannot be pushed.";

  private VersionPublicStore versionPublicStore;
  private VersionPrivateStore versionPrivateStore;
  private ElementPublicStore elementPublicStore;
  private ElementPrivateStore elementPrivateStore;

  public PublishService(VersionPublicStore versionPublicStore,
                        VersionPrivateStore versionPrivateStore,
                        ElementPublicStore elementPublicStore,
                        ElementPrivateStore elementPrivateStore) {
    this.versionPublicStore = versionPublicStore;
    this.versionPrivateStore = versionPrivateStore;
    this.elementPublicStore = elementPublicStore;
    this.elementPrivateStore = elementPrivateStore;
  }

  public CollaborationPublishResult publish(SessionContext context, Id itemId, Id versionId,
                                            String message) {
    CollaborationPublishResult result = new CollaborationPublishResult();
    result.setChange(new CollaborationMergeChange());

    Date publishTime = new Date();
    Id revisionId = new Id(UUID.randomUUID().toString());
    boolean versionFirstPublication = publishVersion(context, itemId, versionId, revisionId,
        publishTime,message);
    if (versionFirstPublication) {
      publishAllElements(context, new ElementContext(itemId, versionId, revisionId), publishTime,
          result);
    } else {
      publishDirtyElements(context, new ElementContext(itemId, versionId, revisionId), publishTime,
          result);
    }
    return result;
  }

  private boolean publishVersion(SessionContext context, Id itemId, Id versionId, Id revisionId,
                                 Date publishTime, String message) {
    SynchronizationStateEntity privateVersionSyncState =
        versionPrivateStore.getSynchronizationState(context, itemId, versionId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format(PUSH_NON_EXISTING_VERSION, itemId.toString(), versionId.toString())));

    if (!privateVersionSyncState.isDirty()) {
      throw new ZusammenException(new ReturnCode(NO_CHANGES_TO_PUBLISH, ZCSP,
          String.format(Message.NO_CHANGES_TO_PUBLISH, itemId, versionId), null));
    }

    Optional<SynchronizationStateEntity> publicVersionSyncState =
        versionPublicStore.getSynchronizationState(context, itemId, versionId);

    // private must be synced with public (if public exists)
    if (publicVersionSyncState.isPresent() &&
        !privateVersionSyncState.getPublishTime()
            .equals(publicVersionSyncState.get().getPublishTime())) {
      // should not happen as it is validated in zusammen-core
      throw new UnsupportedOperationException("Out of sync item version can not be publish");
    }

    boolean versionFirstPublication;
    Map<Id, Id> versionElementIds =
        elementPublicStore.listIds(context, new ElementContext(itemId,
            versionId));
    if (publicVersionSyncState.isPresent()) {
      versionPublicStore.update(context, itemId, new VersionEntity(versionId), revisionId,
          versionElementIds,publishTime,message);
      versionFirstPublication = false;
    } else {
      VersionEntity privateVersion = versionPrivateStore.get(context, itemId, versionId)
          .orElseThrow(() -> new IllegalArgumentException(
              String.format(PUSH_NON_EXISTING_VERSION, itemId.toString(), versionId.toString())));
      versionPublicStore.create(context, itemId, privateVersion, revisionId,versionElementIds,
          publishTime,message);
      versionFirstPublication = true;
    }
    versionPrivateStore.markAsPublished(context, itemId, versionId, publishTime);
    return versionFirstPublication;
  }

  private void publishAllElements(SessionContext context, ElementContext elementContext,
                                  Date publishTime, CollaborationPublishResult result) {
    Collection<SynchronizationStateEntity> privateElementSyncStates =
        elementPrivateStore.listSynchronizationStates(context, elementContext);

    for (SynchronizationStateEntity privateElementSyncState : privateElementSyncStates) {
      Optional<ElementEntity> privateElement =
          elementPrivateStore.get(context, elementContext, privateElementSyncState.getId());

      if (!privateElement.isPresent()) {
        continue;
      }
      ElementEntity elementToPublish = privateElement.get();

      elementPublicStore.create(context, elementContext, elementToPublish,
          privateElementSyncState.isDirty() ? publishTime
              : privateElementSyncState.getPublishTime());

      if (privateElementSyncState.isDirty()) {
        elementPrivateStore
            .markAsPublished(context, elementContext, privateElementSyncState.getId(), publishTime);
      }
      updateResult(elementContext, elementToPublish, Action.CREATE,
          ROOT_ELEMENTS_PARENT_ID.equals(privateElementSyncState.getId()), result);
    }
  }

  private void publishDirtyElements(SessionContext context, ElementContext elementContext,
                                    Date publishTime, CollaborationPublishResult result) {

    Id revisionId = elementContext.getRevisionId();
    elementContext.setRevisionId(revisionId);
    ElementContext privateElementContext = new ElementContext(elementContext.getItemId(),
        elementContext.getVersionId(),Id.ZERO);
    Collection<SynchronizationStateEntity> privateElementSyncStates =
        elementPrivateStore.listSynchronizationStates(context, elementContext);

    Collection<SynchronizationStateEntity> publicElementSyncStates =
        elementPublicStore.listSynchronizationStates(context, elementContext);

    for (SynchronizationStateEntity privateElementSyncState : privateElementSyncStates) {
      if (!privateElementSyncState.isDirty()) {
        continue;
      }

      Optional<ElementEntity> privateElement =
          elementPrivateStore.get(context, privateElementContext, privateElementSyncState.getId());

      ElementEntity elementToPublish;
      Action actionOnPublic;
      if (privateElement.isPresent()) {
        elementToPublish = privateElement.get();

        if (publicElementSyncStates.contains(privateElementSyncState)) {

          elementPublicStore.update(context, elementContext, elementToPublish, publishTime);
          actionOnPublic = Action.UPDATE;
        } else {
          elementPublicStore.create(context, elementContext, elementToPublish, publishTime);
          actionOnPublic = Action.CREATE;
        }

        elementPrivateStore
            .markAsPublished(context, privateElementContext, privateElementSyncState.getId(), publishTime);
      } else {
        elementToPublish =
            elementPublicStore.get(context, elementContext, privateElementSyncState.getId())
                .orElseThrow(() -> new IllegalStateException(
                    "Element that should be deleted from public must exist there"));
        elementPublicStore.delete(context, elementContext, elementToPublish, publishTime);
        actionOnPublic = Action.DELETE;

        elementPrivateStore
            .markDeletionAsPublished(context, privateElementContext, privateElementSyncState.getId(),
                publishTime);
      }

      updateResult(elementContext, elementToPublish, actionOnPublic,
          ROOT_ELEMENTS_PARENT_ID.equals(privateElementSyncState.getId()), result);
    }
  }

  private void updateResult(ElementContext elementContext, ElementEntity element,
                            Action action, boolean versionDataElement,
                            CollaborationPublishResult result) {
    if (versionDataElement) {
      result.getChange().setChangedVersion(convertToVersionChange(elementContext, element, action));
    } else {
      result.getChange().getChangedElements()
          .add(convertToElementChange(elementContext, element, action));
    }
  }
}
