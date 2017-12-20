package org.openecomp.core.zusammen.plugin.collaboration.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.ZusammenPluginConstants;
import org.openecomp.core.zusammen.plugin.collaboration.ElementPrivateStore;
import org.openecomp.core.zusammen.plugin.dao.ElementRepository;
import org.openecomp.core.zusammen.plugin.dao.ElementRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.ElementSynchronizationStateRepository;
import org.openecomp.core.zusammen.plugin.dao.ElementSynchronizationStateRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getPrivateElementContext;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getPrivateSpaceName;

public class ElementPrivateStoreImpl implements ElementPrivateStore {
  private static final Id REVISION_ID = Id.ZERO; // the private revision id is Id.ZERO 0000000...

  @Override
  public Map<Id, Id> listIds(SessionContext context, ElementContext elementContext) {
    return getElementRepository(context)
        .listIds(context, new ElementEntityContext(getPrivateSpaceName(context), elementContext));
  }

  @Override
  public Collection<ElementEntity> listSubs(SessionContext context, ElementContext elementContext,
                                            Id elementId) {
    if (elementId == null) {
      elementId = ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID;
    }

    ElementRepository elementRepository = getElementRepository(context);
    ElementEntityContext privateContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateContext.setRevisionId(REVISION_ID);
    return elementRepository.get(context, privateContext, new ElementEntity(elementId))
        .map(ElementEntity::getSubElementIds).orElse(new HashSet<>()).stream()
        .map(subElementId -> elementRepository
            .get(context, privateContext, new ElementEntity(subElementId)).get())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ElementEntity> get(SessionContext context, ElementContext elementContext,
                                     Id elementId) {
    ElementEntityContext privateElementContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateElementContext.setRevisionId(REVISION_ID);
    return getElementRepository(context)
        .get(context, privateElementContext,
            new ElementEntity(elementId));
  }

  @Override
  public Optional<ElementEntity> getDescriptor(SessionContext context,
                                               ElementContext elementContext, Id elementId) {
    return getElementRepository(context)
        .getDescriptor(context,
            new ElementEntityContext(getPrivateSpaceName(context), getPrivateElementContext
                (elementContext)),
            new ElementEntity(elementId));
  }

  @Override
  public Collection<SynchronizationStateEntity> listSynchronizationStates(SessionContext context,
                                                                          ElementContext elementContext) {
    ElementEntityContext privateElementContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    return getElementSyncStateRepository(context)
        .list(context, privateElementContext);
  }

  @Override
  public Optional<SynchronizationStateEntity> getSynchronizationState(SessionContext context,
                                                                      ElementContext elementContext,
                                                                      Id elementId) {

    ElementEntityContext privateElementContext =
        new ElementEntityContext(getPrivateSpaceName(context), getPrivateElementContext
            (elementContext));
    return getElementSyncStateRepository(context)
        .get(context, privateElementContext,
            new SynchronizationStateEntity(elementId, REVISION_ID));
  }

  @Override
  public void create(SessionContext context, ElementContext elementContext, ElementEntity element) {
    create(context, elementContext, element, true, null);
  }

  @Override
  public boolean update(SessionContext context, ElementContext elementContext,
                        ElementEntity element) {
    ElementEntityContext privateContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateContext.setRevisionId(REVISION_ID);

    if (!isElementChanged(context, privateContext, element)) {
      return false;
    }

    getElementRepository(context).update(context, privateContext, element);
    getElementSyncStateRepository(context).markAsDirty(context, privateContext,
        new SynchronizationStateEntity(element.getId(), REVISION_ID));
    return true;
  }

  @Override
  public void delete(SessionContext context, ElementContext elementContext, ElementEntity element) {

    ElementEntityContext privateElementContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateElementContext.setRevisionId(REVISION_ID);
    deleteElementHierarchy(context, getElementRepository(context),
        getElementSyncStateRepository(context),
        privateElementContext, element);
  }

  @Override
  public void markAsPublished(SessionContext context, ElementContext elementContext, Id elementId,
                              Date publishTime) {
    ElementEntityContext privateContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateContext.setRevisionId(REVISION_ID);
    getElementSyncStateRepository(context).update(context,
        privateContext,
        new SynchronizationStateEntity(elementId, REVISION_ID, publishTime, false));
  }

  @Override
  public void markDeletionAsPublished(SessionContext context, ElementContext elementContext,
                                      Id elementId, Date publishTime) {

    ElementEntityContext privateContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateContext.setRevisionId(REVISION_ID);
    getElementSyncStateRepository(context).delete(context,
        privateContext,
        new SynchronizationStateEntity(elementId, REVISION_ID));
  }

  @Override
  public void commitStagedCreate(SessionContext context, ElementContext elementContext,
                                 ElementEntity element, Date publishTime) {
    create(context, elementContext, element, false, publishTime);
  }

  @Override
  public void commitStagedUpdate(SessionContext context, ElementContext elementContext,
                                 ElementEntity element, Date publishTime) {
    ElementEntityContext privateContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateContext.setRevisionId(REVISION_ID);

    getElementRepository(context).update(context, privateContext, element);
    // Currently Resolution='Other' is not supported so this is invoked after conflict was
    // resolved with Resolution='Theirs' so dirty flag should be turned off.
    // (if there was no conflict it's off anyway)
    getElementSyncStateRepository(context).update(context, privateContext,
        new SynchronizationStateEntity(element.getId(), REVISION_ID, publishTime, false));
  }

  @Override
  public void commitStagedDelete(SessionContext context, ElementContext elementContext,
                                 ElementEntity element) {
    ElementEntityContext privateContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateContext.setRevisionId(REVISION_ID);
    getElementRepository(context).delete(context, privateContext, element);
    getElementSyncStateRepository(context)
        .delete(context, privateContext,
            new SynchronizationStateEntity(element.getId(), REVISION_ID));
  }

  @Override
  public void commitStagedIgnore(SessionContext context, ElementContext elementContext,
                                 ElementEntity element, Date publishTime) {
    // publish time - updated to mark that this element was already synced with this publish time
    // (even though the local data was preferred) and to prevent this conflict again.
    // dirty - turned on because the local data which is different than the public one was
    // preferred. It will enable future publication of this data.
    getElementSyncStateRepository(context).update(context,
        new ElementEntityContext(getPrivateSpaceName(context), elementContext),
        new SynchronizationStateEntity(element.getId(), REVISION_ID, publishTime, true));
  }

  private void create(SessionContext context, ElementContext elementContext,
                      ElementEntity element, boolean dirty, Date publishTime) {
    ElementEntityContext privateContext =
        new ElementEntityContext(getPrivateSpaceName(context), elementContext);
    privateContext.setRevisionId(REVISION_ID);
    getElementRepository(context).create(context, privateContext, element);
    getElementSyncStateRepository(context).create(context, privateContext,
        new SynchronizationStateEntity(element.getId(), REVISION_ID, publishTime, dirty));
  }


  private void deleteElementHierarchy(
      SessionContext context, ElementRepository elementRepository,
      ElementSynchronizationStateRepository elementSyncStateRepository,
      ElementEntityContext elementContext, ElementEntity element) {

    Optional<ElementEntity> retrieved = elementRepository.get(context, elementContext, element);
    if (!retrieved.isPresent()) {
      return;
    }
    retrieved.get().getSubElementIds().stream()
        .map(ElementEntity::new)
        .forEach(subElementEntity -> deleteElementHierarchy(
            context, elementRepository, elementSyncStateRepository, elementContext,
            subElementEntity));

    // only for the first one the parentId will populated (so it'll be removed from its parent)
    elementRepository.delete(context, elementContext, element);
    handleDeletedElementSyncState(context, elementSyncStateRepository, elementContext, element);
  }

  private void handleDeletedElementSyncState(SessionContext context,
                                             ElementSynchronizationStateRepository elementSyncStateRepository,
                                             ElementEntityContext elementContext,
                                             ElementEntity element) {
    SynchronizationStateEntity elementSyncState = new SynchronizationStateEntity(element.getId(),
        REVISION_ID);
    if (elementSyncStateRepository.get(context, elementContext, elementSyncState).
        orElseThrow(
            () -> new IllegalStateException("Synchronization state must exist for an element"))
        .getPublishTime() == null) {
      elementSyncStateRepository.delete(context, elementContext, elementSyncState);
    } else {
      elementSyncStateRepository.markAsDirty(context, elementContext, elementSyncState);
    }
  }

  private boolean isElementChanged(SessionContext context,
                                   ElementEntityContext elementContext,
                                   ElementEntity newElement) {
    return getElementHash(context, elementContext, new ElementEntity(newElement.getId()))
        .map(existingHash -> !newElement.getElementHash().equals(existingHash))
        .orElse(true);
  }

  private Optional<Id> getElementHash(SessionContext context,
                                      ElementEntityContext elementEntityContext,
                                      ElementEntity element) {
    return getElementRepository(context).getHash(context, elementEntityContext, element);
  }

  protected ElementRepository getElementRepository(SessionContext context) {
    return ElementRepositoryFactory.getInstance().createInterface(context);
  }

  protected ElementSynchronizationStateRepository getElementSyncStateRepository(
      SessionContext context) {
    return ElementSynchronizationStateRepositoryFactory.getInstance().createInterface(context);
  }

}
