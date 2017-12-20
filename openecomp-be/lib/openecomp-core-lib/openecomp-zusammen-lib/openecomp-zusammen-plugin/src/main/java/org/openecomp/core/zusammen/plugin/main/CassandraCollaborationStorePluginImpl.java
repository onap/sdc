/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.zusammen.plugin.main;

import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.commons.health.data.HealthStatus;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Revision;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import com.amdocs.zusammen.datatypes.response.ErrorCode;
import com.amdocs.zusammen.datatypes.response.Module;
import com.amdocs.zusammen.datatypes.response.Response;
import com.amdocs.zusammen.datatypes.response.ReturnCode;
import com.amdocs.zusammen.datatypes.response.ZusammenException;
import com.amdocs.zusammen.sdk.collaboration.CollaborationStore;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElementChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElementConflict;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationItemVersionConflict;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;
import com.amdocs.zusammen.sdk.types.ElementConflictDescriptor;
import com.amdocs.zusammen.sdk.types.ElementDescriptor;
import org.openecomp.core.zusammen.plugin.ZusammenPluginUtil;
import org.openecomp.core.zusammen.plugin.collaboration.CommitStagingService;
import org.openecomp.core.zusammen.plugin.collaboration.ElementPrivateStore;
import org.openecomp.core.zusammen.plugin.collaboration.ElementPublicStore;
import org.openecomp.core.zusammen.plugin.collaboration.ElementStageStore;
import org.openecomp.core.zusammen.plugin.collaboration.PublishService;
import org.openecomp.core.zusammen.plugin.collaboration.RevertService;
import org.openecomp.core.zusammen.plugin.collaboration.SyncService;
import org.openecomp.core.zusammen.plugin.collaboration.VersionPrivateStore;
import org.openecomp.core.zusammen.plugin.collaboration.VersionPublicStore;
import org.openecomp.core.zusammen.plugin.collaboration.VersionStageStore;
import org.openecomp.core.zusammen.plugin.collaboration.impl.ElementPrivateStoreImpl;
import org.openecomp.core.zusammen.plugin.collaboration.impl.ElementPublicStoreImpl;
import org.openecomp.core.zusammen.plugin.collaboration.impl.ElementStageStoreImpl;
import org.openecomp.core.zusammen.plugin.collaboration.impl.VersionPrivateStoreImpl;
import org.openecomp.core.zusammen.plugin.collaboration.impl.VersionPublicStoreImpl;
import org.openecomp.core.zusammen.plugin.collaboration.impl.VersionStageStoreImpl;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionDataElement;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.amdocs.zusammen.datatypes.item.SynchronizationStatus.MERGING;
import static com.amdocs.zusammen.datatypes.item.SynchronizationStatus.OUT_OF_SYNC;
import static com.amdocs.zusammen.datatypes.item.SynchronizationStatus.UP_TO_DATE;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToCollaborationElement;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToElementChange;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToElementDescriptor;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToElementEntity;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToItemVersion;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToVersionData;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToVersionEntity;

public class CassandraCollaborationStorePluginImpl implements CollaborationStore {
  // TODO: 8/15/2017 inject

  private VersionPrivateStore versionPrivateStore = new VersionPrivateStoreImpl();
  private VersionPublicStore versionPublicStore = new VersionPublicStoreImpl();
  private VersionStageStore versionStageStore = new VersionStageStoreImpl();

  private ElementPrivateStore elementPrivateStore = new ElementPrivateStoreImpl();
  private ElementPublicStore elementPublicStore = new ElementPublicStoreImpl();
  private ElementStageStore elementStageStore = new ElementStageStoreImpl();

  // TODO: 9/4/2017
  private CommitStagingService commitStagingService =
      new CommitStagingService(versionPrivateStore, versionStageStore, elementPrivateStore,
          elementStageStore);
  private PublishService publishService =
      new PublishService(versionPublicStore, versionPrivateStore, elementPublicStore,
          elementPrivateStore);
  private SyncService syncService =
      new SyncService(versionPublicStore, versionPrivateStore, versionStageStore,
          elementPublicStore, elementPrivateStore, elementStageStore);

  private RevertService revertService =
      new RevertService(elementPublicStore, elementPrivateStore);

  @Override
  public Response<Void> createItem(SessionContext context, Id itemId, Info info) {
    // done by state store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteItem(SessionContext context, Id itemId) {
    // done by state store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> createItemVersion(SessionContext context, Id itemId, Id baseVersionId,
                                          Id versionId, ItemVersionData itemVersionData) {
    Date creationTime = new Date();
    versionPrivateStore.create(context, itemId,
        convertToVersionEntity(versionId, baseVersionId, creationTime, creationTime));

    ElementContext elementContext = new ElementContext(itemId, versionId);
    VersionDataElement versionData = new VersionDataElement(itemVersionData);

    if (baseVersionId == null) {
      elementPrivateStore.create(context, elementContext, versionData);
    } else {
      copyElements(context, new ElementContext(itemId, baseVersionId), elementContext);
      elementPrivateStore.update(context, elementContext, versionData);
    }

    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateItemVersion(SessionContext context, Id itemId, Id versionId,
                                          ItemVersionData itemVersionData) {

    if (elementPrivateStore.update(context, new ElementContext(itemId, versionId),
        new VersionDataElement(itemVersionData))) {

      VersionEntity version = new VersionEntity(versionId);
      version.setModificationTime(new Date());
      versionPrivateStore.update(context, itemId, version);
    }

    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    elementPrivateStore
        .delete(context, new ElementContext(itemId, versionId), new VersionDataElement());

    versionPrivateStore.delete(context, itemId, new VersionEntity(versionId));
    return new Response(Void.TYPE);
  }

  @Override
  public Response<ItemVersionStatus> getItemVersionStatus(SessionContext context, Id itemId,
                                                          Id versionId) {
    if (versionStageStore.get(context, itemId, new VersionEntity(versionId)).isPresent()) {
      return new Response<>(new ItemVersionStatus(MERGING, true));
    }

    Optional<SynchronizationStateEntity> publicSyncState =
        versionPublicStore.getSynchronizationState(context, itemId, versionId);

    if (!publicSyncState.isPresent()) {
      return new Response<>(new ItemVersionStatus(UP_TO_DATE, true));
    }

    SynchronizationStateEntity privateSyncState =
        versionPrivateStore.getSynchronizationState(context, itemId, versionId)
            // TODO: 7/18/2017 ?
            .orElseThrow(() -> new IllegalStateException("private version must exist"));

    return new Response<>(new ItemVersionStatus(
        privateSyncState.getPublishTime().equals(publicSyncState.get().getPublishTime())
            ? UP_TO_DATE
            : OUT_OF_SYNC,
        privateSyncState.isDirty()));
  }

  @Override
  public Response<Void> tagItemVersion(SessionContext context, Id itemId, Id versionId,
                                       Id revisionId,
                                       Tag tag) {
   /* if (revisionId != null) {
      throw new UnsupportedOperationException(
          "In this plugin implementation tag is supported only on versionId");
    }

    copyElements(context,
        new ElementContext(itemId, versionId),
        new ElementContext(itemId, versionId, tag.getName()));*/

    return new Response(Void.TYPE);
  }

  @Override
  public Response<CollaborationPublishResult> publishItemVersion(SessionContext context,
                                                                 Id itemId, Id versionId,
                                                                 String message) {
    try {
      return new Response<>(publishService.publish(context, itemId, versionId, message));
    } catch (ZusammenException ze) {
      return new Response<>(
          new ReturnCode(ErrorCode.CL_ITEM_VERSION_PUBLISH, Module.ZCSP, null, ze.getReturnCode()));
    }
  }

  @Override
  public Response<CollaborationMergeResult> syncItemVersion(SessionContext context, Id itemId,
                                                            Id versionId) {
    CollaborationMergeResult result = syncService.sync(context, itemId, versionId, false);
    commitStagingService.commitStaging(context, itemId, versionId);

    return new Response<>(result);
  }

  @Override
  public Response<CollaborationMergeResult> forceSyncItemVersion(SessionContext context, Id itemId,
                                                                 Id versionId) {
    CollaborationMergeResult result = syncService.sync(context, itemId, versionId, true);
    commitStagingService.commitStaging(context, itemId, versionId);

    return new Response<>(result);
  }

  @Override
  public Response<CollaborationMergeResult> mergeItemVersion(SessionContext context, Id itemId,
                                                             Id versionId, Id sourceVersionId) {
    throw new UnsupportedOperationException("mergeItemVersion");
  }

  @Override
  public Response<CollaborationItemVersionConflict> getItemVersionConflict(SessionContext context,
                                                                           Id itemId,
                                                                           Id versionId) {
    ElementContext elementContext = new ElementContext(itemId, versionId, Id.ZERO);

    Collection<StageEntity<ElementEntity>> conflictedStagedElementDescriptors =
        elementStageStore.listConflictedDescriptors(context, elementContext);

    CollaborationItemVersionConflict result = new CollaborationItemVersionConflict();
    for (StageEntity<ElementEntity> stagedElementDescriptor : conflictedStagedElementDescriptors) {
      if (ROOT_ELEMENTS_PARENT_ID.equals(stagedElementDescriptor.getEntity().getId())) {
        result.setVersionDataConflict(
            getVersionDataConflict(context, elementContext, stagedElementDescriptor));
      } else {
        result.getElementConflictDescriptors()
            .add(getElementConflictDescriptor(context, elementContext, stagedElementDescriptor));
      }
    }
    return new Response<>(result);
  }

  @Override
  public Response<ItemVersionRevisions> listItemVersionRevisions(SessionContext context, Id itemId,
                                                                 Id versionId) {
    return new Response<>(versionPublicStore.listItemVersionRevisions(context, itemId, versionId));
  }

  @Override
  public Response<Revision> getItemVersionRevision(SessionContext context, Id itemId, Id versionId,
                                                   Id revisionId) {
    throw new UnsupportedOperationException(
        "get revision is not supported in the current cassandra plugin");
  }

  @Override
  public Response<CollaborationMergeChange> resetItemVersionRevision(SessionContext context,
                                                                     Id itemId, Id versionId,
                                                                     Id revisionId) {
    throw new UnsupportedOperationException("resetItemVersionRevision function not supported");

  }

  @Override
  public Response<CollaborationMergeChange> revertItemVersionRevision(SessionContext context,
                                                                      Id itemId, Id versionId,
                                                                      Id revisionId) {
    Optional<ItemVersion> itemVersion = getItemVersion(context, itemId, versionId, revisionId);
    if (!itemVersion.isPresent()) {
      throw new RuntimeException(String
          .format("Item %s, version %s: Cannot revert to revision %s since it is not found",
              itemId, versionId, revisionId));
    }

    // TODO: 12/4/2017 force sync is done in order to clear dirty element on private
    // this is temp solution that should be fixed.
    forceSyncItemVersion(context, itemId, versionId);

    //updateItemVersion(context, itemId, versionId, itemVersion.get().getData());
    revertService.revert(context, itemId, versionId, revisionId);

    return new Response<>(new CollaborationMergeChange());
  }


  @Override
  public Response<Void> commitElements(SessionContext context, Id itemId, Id versionId, String s) {
    // not needed
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Collection<CollaborationElement>> listElements(SessionContext context,
                                                                 ElementContext elementContext,
                                                                 Namespace namespace,
                                                                 Id elementId) {
    return new Response<>(elementPrivateStore.listSubs(context, elementContext, elementId).stream()
        .map(elementEntity -> convertToCollaborationElement(elementContext, elementEntity))
        .collect(Collectors.toList()));
  }

  @Override
  public Response<CollaborationElement> getElement(SessionContext context,
                                                   ElementContext elementContext,
                                                   Namespace namespace, Id elementId) {
    return new Response<>(elementPrivateStore.get(context, elementContext, elementId)
        .map(elementEntity -> convertToCollaborationElement(elementContext, elementEntity))
        .orElse(null));
  }

  @Override
  public Response<CollaborationElementConflict> getElementConflict(SessionContext context,
                                                                   ElementContext elementContext,
                                                                   Namespace namespace,
                                                                   Id elementId) {
    Optional<StageEntity<ElementEntity>> conflictedStagedElement =
        elementStageStore
            .getConflicted(context, elementContext, new ElementEntity(elementId));

    return new Response<>(conflictedStagedElement
        .map(stagedElement -> getElementConflict(context, elementContext, stagedElement))
        .orElse(null));
  }

  @Override
  public Response<Void> createElement(SessionContext context, CollaborationElement element) {
    elementPrivateStore.create(context,
        new ElementContext(element.getItemId(), element.getVersionId()),
        convertToElementEntity(element));
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateElement(SessionContext context, CollaborationElement element) {
    elementPrivateStore.update(context,
        new ElementContext(element.getItemId(), element.getVersionId()),
        convertToElementEntity(element));
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteElement(SessionContext context, CollaborationElement element) {
    elementPrivateStore
        .delete(context, new ElementContext(element.getItemId(), element.getVersionId()),
            convertToElementEntity(element));

    return new Response(Void.TYPE);
  }

  @Override
  public Response<CollaborationMergeResult> resolveElementConflict(SessionContext context,
                                                                   CollaborationElement element,
                                                                   Resolution resolution) {
    ElementContext elementContext = new ElementContext(element.getItemId(), element.getVersionId());
    elementStageStore
        .resolveConflict(context, elementContext, convertToElementEntity(element), resolution);
    commitStagingService.commitStaging(context, element.getItemId(), element.getVersionId());

    return new Response<>(new CollaborationMergeResult());
  }

  @Override
  public Response<ItemVersion> getItemVersion(SessionContext context, Space space, Id itemId,
                                              Id versionId, Id revisionId) {
    return new Response<>(getItemVersion(context, itemId, versionId, revisionId).orElse(null));
  }

  @Override
  public Response<HealthInfo> checkHealth(SessionContext context) throws ZusammenException {
    HealthInfo healthInfo = versionPublicStore.checkHealth(context)
        ? new HealthInfo(Module.ZCSP.getDescription(), HealthStatus.UP, "")
        : new HealthInfo(Module.ZCSP.getDescription(), HealthStatus.DOWN, "No Schema Available");

    return new Response<>(healthInfo);
  }

  private Optional<ItemVersion> getItemVersion(SessionContext context, Id itemId, Id versionId,
                                               Id revisionId) {
    // since revisions are kept only on public - get from there
    Optional<VersionEntity> versionEntity = versionPublicStore.get(context, itemId, versionId);
    if (!versionEntity.isPresent()) {
      return Optional.empty();
    }

    return elementPublicStore
        .getDescriptor(context, new ElementContext(itemId, versionId, revisionId),
            ROOT_ELEMENTS_PARENT_ID)
        .map(ZusammenPluginUtil::convertToVersionData)
        .map(itemVersionData -> convertToItemVersion(versionEntity.get(), itemVersionData));
  }

  private List<ElementEntity> listVersionElements(SessionContext context,
                                                  ElementContext elementContext) {
    return elementPrivateStore.listIds(context, elementContext).entrySet().stream() // TODO:
        // 9/5/2017 parallel
        .map(entry -> elementPrivateStore.get(context, elementContext, entry.getKey()).get())
        .collect(Collectors.toList());
  }

  private void copyElements(SessionContext context,
                            ElementContext sourceContext, ElementContext targetContext) {
    listVersionElements(context, sourceContext).forEach(element -> {
      // publishTime copied as is and dirty is off
      Date publishTime =
          elementPrivateStore.getSynchronizationState(context, sourceContext, element.getId())
              .get().getPublishTime();
      elementPrivateStore.commitStagedCreate(context, targetContext, element, publishTime);
    });
  }

  private ItemVersionDataConflict getVersionDataConflict(SessionContext context,
                                                         ElementContext elementContext,
                                                         StageEntity<ElementEntity> stagedElementDescriptor) {
    ItemVersionDataConflict versionConflict = new ItemVersionDataConflict();
    versionConflict.setRemoteData(convertToVersionData(stagedElementDescriptor.getEntity()));
    if (stagedElementDescriptor.getAction() == Action.UPDATE) {
      versionConflict.setLocalData(getPrivateVersionData(context, elementContext));
    }
    return versionConflict;
  }

  private ItemVersionData getPrivateVersionData(SessionContext context,
                                                ElementContext elementContext) {
    return elementPrivateStore.getDescriptor(context, elementContext, ROOT_ELEMENTS_PARENT_ID)
        .map(ZusammenPluginUtil::convertToVersionData)
        .orElseThrow(() -> new IllegalStateException("Version must have data"));
  }

  private ElementConflictDescriptor getElementConflictDescriptor(SessionContext context,
                                                                 ElementContext elementContext,
                                                                 StageEntity<ElementEntity> stagedElementDescriptor) {
    ElementDescriptor elementDescriptorFromStage =
        convertToElementDescriptor(elementContext, (stagedElementDescriptor.getEntity()));

    ElementConflictDescriptor conflictDescriptor = new ElementConflictDescriptor();
    switch (stagedElementDescriptor.getAction()) {
      case CREATE:
        conflictDescriptor.setRemoteElementDescriptor(elementDescriptorFromStage);
        break;
      case UPDATE:
        conflictDescriptor.setRemoteElementDescriptor(elementDescriptorFromStage);
        conflictDescriptor.setLocalElementDescriptor(convertToElementDescriptor(elementContext,
            elementPrivateStore
                .getDescriptor(context, elementContext, stagedElementDescriptor.getEntity().getId())
                .orElse(null)));// updated on public while deleted from private
        break;
      case DELETE:
        conflictDescriptor.setLocalElementDescriptor(elementDescriptorFromStage);
        break;
      default:
        break;
    }
    return conflictDescriptor;
  }

  private void addElementsToChangedElements(ElementContext elementContext,
                                            Collection<ElementEntity> elements,
                                            Collection<CollaborationElementChange> changedElements,
                                            Action action) {
    elements.stream()
        .map(elementEntity -> convertToElementChange(elementContext, elementEntity, action))
        .forEach(changedElements::add);
  }

  private CollaborationElementConflict getElementConflict(SessionContext context,
                                                          ElementContext entityContext,
                                                          StageEntity<ElementEntity> stagedElement) {
    CollaborationElement elementFromStage =
        convertToCollaborationElement(entityContext, (stagedElement.getEntity()));

    CollaborationElementConflict conflict = new CollaborationElementConflict();
    switch (stagedElement.getAction()) {
      case CREATE:
        conflict.setRemoteElement(elementFromStage);
        break;
      case UPDATE:
        conflict.setRemoteElement(elementFromStage);
        conflict.setLocalElement(
            elementPrivateStore.get(context, entityContext, stagedElement.getEntity().getId())
                .map(element -> convertToCollaborationElement(entityContext, element))
                .orElse(null));// updated on public while deleted from private
        break;
      case DELETE:
        conflict.setLocalElement(elementFromStage);
        break;
      default:
        break;
    }
    return conflict;
  }
}