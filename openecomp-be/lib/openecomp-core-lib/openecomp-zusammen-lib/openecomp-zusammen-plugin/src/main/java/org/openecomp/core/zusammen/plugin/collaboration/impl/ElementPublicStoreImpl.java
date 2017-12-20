package org.openecomp.core.zusammen.plugin.collaboration.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.collaboration.ElementPublicStore;
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
import java.util.Optional;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getSpaceName;

public class ElementPublicStoreImpl implements ElementPublicStore {

  @Override
  public Optional<ElementEntity> get(SessionContext context,
                                     ElementContext elementContext, Id elementId) {
    return getElementRepository(context)
        .get(context, new ElementEntityContext(getSpaceName(context, Space.PUBLIC), elementContext),
            new ElementEntity(elementId));
  }

  @Override
  public Optional<ElementEntity> getDescriptor(SessionContext context,
                                               ElementContext elementContext, Id elementId) {
    return getElementRepository(context).getDescriptor(context,
        new ElementEntityContext(getSpaceName(context, Space.PUBLIC), elementContext),
        new ElementEntity(elementId));
  }

  @Override
  public Collection<SynchronizationStateEntity> listSynchronizationStates(
      SessionContext context, ElementContext elementContext) {
    ElementEntityContext entityContext = new ElementEntityContext(getSpaceName
        (context, Space.PUBLIC), elementContext);

    ElementSynchronizationStateRepository elementSyncStateRepository =
        getElementSyncStateRepository(context);
    Map<Id, Id> ids = getElementRepository(context).listIds(context, entityContext);

    Collection<SynchronizationStateEntity> synchronizationStateEntities = new HashSet<>();
    for (Map.Entry<Id, Id> elementEntry : ids.entrySet()) {
      Optional<SynchronizationStateEntity> synchronizationStateEntity = elementSyncStateRepository.
          get(context, entityContext, new SynchronizationStateEntity(elementEntry.getKey(),
              elementEntry.getValue()));
      if (synchronizationStateEntity.isPresent()) {
        synchronizationStateEntities.add(synchronizationStateEntity.get());
      } else {
        /*throw new IllegalStateException(String.format(
            "list Synchronization States error: " + "element %s revision %s, which appears as an " +
                "element of " +
                "item" +
                " %s version %s, does not exist",
            elementEntry.getKey(), elementEntry.getValue(), elementContext.getItemId().getValue(),
            elementContext.getVersionId().getValue()));*/
      }
    }

    return synchronizationStateEntities;
  }

  @Override
  public void create(SessionContext context, ElementContext elementContext,
                     ElementEntity element, Date publishTime) {
    ElementEntityContext publicContext =
        new ElementEntityContext(getSpaceName(context, Space.PUBLIC), elementContext);


    if (element.getParentId() != null) {
      createParentElement(context, elementContext, element.getParentId(), publishTime);
    }
    getElementRepository(context).create(context, publicContext, element);
    getElementSyncStateRepository(context).create(context, publicContext,
        new SynchronizationStateEntity(element.getId(), elementContext.getRevisionId(),
            publishTime, false));
  }

  @Override
  public void update(SessionContext context, ElementContext elementContext,
                     ElementEntity element, Date publishTime) {
    //todo - update in public should be create new entry with new revision_id in public - this is a
    // new revision
    ElementEntityContext publicContext =
        new ElementEntityContext(getSpaceName(context, Space.PUBLIC), elementContext);

    Optional<ElementEntity> publicElement = getElementRepository(context).get(context,
        publicContext, new ElementEntity(element.getId()));
    if (publicElement.isPresent()) {
      getElementRepository(context).update(context, publicContext, element);
    } else {
       publicElement = get(context,new ElementContext(publicContext.getItemId(),publicContext
           .getVersionId()),element.getId());
       element.setSubElementIds(publicElement.get().getSubElementIds());
      getElementRepository(context).create(context, publicContext, element);
    }
    getElementSyncStateRepository(context).update(context, publicContext,
        new SynchronizationStateEntity(element.getId(), elementContext.getRevisionId(), publishTime,
            false));
  }

  @Override
  public void delete(SessionContext context, ElementContext elementContext,
                     ElementEntity element, Date publishTime) {
    ElementEntityContext publicContext =
        new ElementEntityContext(getSpaceName(context, Space.PUBLIC), elementContext);

    if (element.getParentId() != null) {
      Optional<ElementEntity> parentElement = get(context, elementContext, element.getParentId());
      if (parentElement.isPresent()) {
        createParentElement(context, elementContext, element.getParentId(), publishTime);
      }
    }

    getElementRepository(context).delete(context, publicContext, element);
    getElementSyncStateRepository(context)
        .delete(context, publicContext, new SynchronizationStateEntity(element.getId(),
            elementContext.getRevisionId()));
  }

  @Override
  public Map<Id, Id> listIds(SessionContext context, ElementContext elementContext) {

    return getElementRepository(context)
        .listIds(context,
            new ElementEntityContext(getSpaceName(context, Space.PUBLIC), elementContext));

  }

  private void createParentElement(SessionContext context, ElementContext elementContext,
                                   Id parentElementId, Date publishTime
  ) {
    ElementEntityContext publicContext =
        new ElementEntityContext(getSpaceName(context, Space.PUBLIC), elementContext);

    Optional<ElementEntity> parentElement =
        getElementRepository(context).get(context, new ElementEntityContext
                (publicContext.getSpace(), publicContext.getItemId(), publicContext.getVersionId(),
                    elementContext.getRevisionId()),
            new ElementEntity(parentElementId));
    if(parentElement.isPresent()) {
      update(context, elementContext, parentElement.get(), publishTime);
    }


   /* Id elementRevisionId = getElementRevision(context, publicContext, elementContext.getRevisionId()
        , parentElementId);

    if (elementRevisionId != null && !elementRevisionId.equals(elementContext.getRevisionId())) {
      Optional<ElementEntity> parentElement =
          getElementRepository(context).get(context, new ElementEntityContext
                  (publicContext.getSpace(), publicContext.getItemId(), publicContext.getVersionId(),
                      elementContext.getRevisionId()),
              new ElementEntity(parentElementId));
      elementRevisionId = getElementRevision(context, publicContext, elementContext.getRevisionId()
          , parentElement.get().getId());
      if (elementRevisionId != null) {
        update(context, elementContext, parentElement.get(), publishTime);
      } else {
        create(context, elementContext, parentElement.get(), publishTime);
      }

    }*/
  }




  protected ElementRepository getElementRepository(SessionContext context) {
    return ElementRepositoryFactory.getInstance().createInterface(context);
  }

  protected ElementSynchronizationStateRepository getElementSyncStateRepository(
      SessionContext context) {
    return ElementSynchronizationStateRepositoryFactory.getInstance().createInterface(context);
  }


}
