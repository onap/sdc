package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElementChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import org.openecomp.core.zusammen.plugin.ZusammenPluginUtil;
import org.openecomp.core.zusammen.plugin.dao.ElementRepository;
import org.openecomp.core.zusammen.plugin.dao.ElementRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getSpaceName;

public class VersionCollaborationStore {

  public void tagItemVersion(SessionContext context, Id itemId, Id versionId, Id changeId,
                             Tag tag) {
    if (changeId != null) {
      throw new UnsupportedOperationException(
          "In this plugin implementation tag is supported only on versionId");
    }
    String space = getSpaceName(context, Space.PRIVATE);
    ElementEntityContext targetContext = new ElementEntityContext(space, itemId, versionId);
    targetContext.setChangeRef(tag.getName());
    copyElements(context, new ElementEntityContext(space, itemId, versionId), targetContext,
        getElementRepository(context));
  }

  public CollaborationMergeChange resetItemVersionHistory(SessionContext context, Id itemId,
                                                          Id versionId, String changeRef) {
    ElementRepository elementRepository = getElementRepository(context);

    String spaceName = getSpaceName(context, Space.PRIVATE);
    ElementEntityContext versionContext = new ElementEntityContext(spaceName, itemId, versionId);

    Collection<ElementEntity> deletedElements =
        deleteElements(context, versionContext, elementRepository);

    ElementEntityContext changeRefContext = new ElementEntityContext(spaceName, itemId, versionId);
    changeRefContext.setChangeRef(changeRef);

    Collection<ElementEntity> createdElements =
        copyElements(context, changeRefContext, versionContext, elementRepository);

    // TODO: 4/19/2017 version change...
    return createCollaborationMergeChange(versionContext, deletedElements, createdElements);
  }

  private Collection<ElementEntity> deleteElements(SessionContext context,
                                                   ElementEntityContext elementContext,
                                                   ElementRepository elementRepository) {
    Collection<ElementEntity> elements = elementRepository.list(context, elementContext);
    elements.forEach(element -> elementRepository
        .delete(context, elementContext, new ElementEntity(element.getId())));
    elementRepository.delete(context, elementContext, new ElementEntity(ROOT_ELEMENTS_PARENT_ID));
    return elements;
  }

  private Collection<ElementEntity> copyElements(SessionContext context,
                                                 ElementEntityContext sourceElementContext,
                                                 ElementEntityContext targetElementContext,
                                                 ElementRepository elementRepository) {
    Collection<ElementEntity> elements = elementRepository.list(context, sourceElementContext);
    elements.forEach(elementEntity ->
        elementRepository.create(context, targetElementContext, elementEntity));
    return elements;
  }

  private CollaborationMergeChange createCollaborationMergeChange(
      ElementEntityContext versionContext,
      Collection<ElementEntity> deletedElements,
      Collection<ElementEntity> createdElements) {
    CollaborationMergeChange mergeChange = new CollaborationMergeChange();
    mergeChange.getChangedElements().addAll(
        convertToCollaborationElementChanges(versionContext, deletedElements, Action.DELETE));
    mergeChange.getChangedElements().addAll(
        convertToCollaborationElementChanges(versionContext, createdElements, Action.CREATE));
    return mergeChange;
  }

  private List<CollaborationElementChange> convertToCollaborationElementChanges(
      ElementEntityContext elementContext, Collection<ElementEntity> changedElements,
      Action action) {
    return changedElements.stream()
        .map(element -> convertToCollaborationElementChange(element, elementContext, action))
        .collect(Collectors.toList());
  }

  private CollaborationElementChange convertToCollaborationElementChange(
      ElementEntity elementEntity, ElementEntityContext elementContext, Action action) {
    CollaborationElementChange elementChange = new CollaborationElementChange();
    elementChange
        .setElement(ZusammenPluginUtil.getCollaborationElement(elementContext, elementEntity));
    elementChange.setAction(action);
    return elementChange;
  }

  protected ElementRepository getElementRepository(SessionContext context) {
    return ElementRepositoryFactory.getInstance().createInterface(context);
  }
}
