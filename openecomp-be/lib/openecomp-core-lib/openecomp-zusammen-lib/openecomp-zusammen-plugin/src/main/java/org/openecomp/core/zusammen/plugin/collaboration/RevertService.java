package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RevertService {

  private ElementPublicStore elementPublicStore;
  private ElementPrivateStore elementPrivateStore;

  public RevertService(ElementPublicStore elementPublicStore,
                       ElementPrivateStore elementPrivateStore) {
    this.elementPublicStore = elementPublicStore;
    this.elementPrivateStore = elementPrivateStore;
  }

  public void revert(SessionContext context, Id itemId, Id versionId, Id revisionId) {
    ElementContext targetContext = new ElementContext(itemId, versionId);
    ElementContext sourceContext = new ElementContext(itemId, versionId, revisionId);
    copyElementsFromPublic(context, sourceContext, targetContext);
  }

  private void copyElementsFromPublic(SessionContext context, ElementContext sourceContext,
                                      ElementContext targetContext) {
    Collection<RevertElementAction> revertElementActions =
        evaluateRevertElementActions(context, sourceContext, targetContext);

    revertElementActions.forEach(revertElementAction -> revertElementAction.run(context));
  }

  private Collection<RevertElementAction> evaluateRevertElementActions(SessionContext context,
                                                                       ElementContext sourceContext,
                                                                       ElementContext targetContext) {

    Map<Id, Id> sourceElements = elementPublicStore.listIds(context, sourceContext);
    Map<Id, Id> targetPublicElements = elementPublicStore.listIds(context, targetContext);
    Collection<SynchronizationStateEntity> synchronizationStateEntities =
        elementPrivateStore.listSynchronizationStates(context, targetContext);

    Map<Id, Id> targetElements =
        evaluateTargetElements(targetPublicElements, synchronizationStateEntities);


    Collection<RevertElementAction> revertElementActions = new ArrayList<>();

    sourceElements.entrySet().forEach(entry -> {
      Id sourceElementId = entry.getKey();
      Id sourceElementRevisionId = entry.getValue();

      if (!targetElements.containsKey(sourceElementId)) {
        revertElementActions
            .add(new RevertElementAction(sourceContext, sourceElementId, commands[CREATE]));
      } else if (!targetElements.get(sourceElementId).equals(sourceElementRevisionId)) {
        revertElementActions
            .add(new RevertElementAction(sourceContext, sourceElementId, commands[UPDATE]));
      }
    });

    targetElements.entrySet().forEach(entry -> {
      Id targetElementId = entry.getKey();
      if (!sourceElements.containsKey(targetElementId)) {
        revertElementActions
            .add(new RevertElementAction(targetContext, targetElementId, commands[DELETE]));
      }
    });

    return revertElementActions;
  }

  private Map<Id, Id> evaluateTargetElements(Map<Id, Id> targetPublicElements,
                                             Collection<SynchronizationStateEntity> syncStates) {
    Map<Id, Id> targetElements = new HashMap<>(targetPublicElements);
    syncStates.stream()
        .filter(SynchronizationStateEntity::isDirty)
        .forEach(syncState -> targetElements.put(syncState.getId(), Id.ZERO));
    return targetElements;
  }

  private static class RevertElementAction {
    private ElementContext elementContext;
    private Id elementId;
    private ActionCommand command;

    private RevertElementAction(ElementContext elementContext, Id elementId,
                                ActionCommand command) {
      this.elementContext = elementContext;
      this.elementId = elementId;
      this.command = command;
    }

    public ElementContext getElementContext() {
      return elementContext;
    }

    public Id getElementId() {
      return elementId;
    }

    public void run(SessionContext context) {
      command.run(context, elementContext, elementId);
    }
  }

  private interface ActionCommand {
    void run(SessionContext context, ElementContext elementContext, Id elementId);
  }

  private static int CREATE = 0;
  private static int UPDATE = 1;
  private static int DELETE = 2;

  private ActionCommand[] commands = {new ActionCommand() {
    @Override
    public void run(SessionContext context, ElementContext elementContext, Id elementId) {
      //create
      Optional<ElementEntity> element = elementPublicStore.get(context, elementContext, elementId);
      if (!element.isPresent()) {
        throw getMissingElementException(elementContext, elementId);
      }
      elementPrivateStore.create(context, elementContext, element.get());
    }
  }, new ActionCommand() {
    @Override
    public void run(SessionContext context, ElementContext elementContext, Id elementId) {
      //update
      Optional<ElementEntity> element = elementPublicStore.get(context, elementContext, elementId);
      if (!element.isPresent()) {
        throw getMissingElementException(elementContext, elementId);
      }
      elementPrivateStore.update(context, elementContext, element.get());
    }
  }, new ActionCommand() {
    @Override
    public void run(SessionContext context, ElementContext elementContext, Id elementId) {
      //delete
      Optional<ElementEntity> element = elementPrivateStore.get(context, elementContext, elementId);
      if (!element.isPresent()) {
        return; // deleted by parent when hierarchy was deleted
      }
      elementPrivateStore.delete(context, elementContext, element.get());
    }
  }};

  private RuntimeException getMissingElementException(ElementContext elementContext,
                                                      Id elementId) {
    return new IllegalStateException(
        String.format("Item Id %s, version Id %s, revision Id %s: Missing element with Id %s",
            elementContext.getItemId().getValue(), elementContext.getVersionId().getValue(),
            elementContext.getRevisionId().getValue(), elementId.getValue())
    );
  }
}
