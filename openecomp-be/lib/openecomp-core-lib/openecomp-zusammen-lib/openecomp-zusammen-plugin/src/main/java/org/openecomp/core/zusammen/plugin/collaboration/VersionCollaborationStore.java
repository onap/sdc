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

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getSpaceName;

public class VersionCollaborationStore {

  public void tagItemVersion(SessionContext context, Id itemId, Id versionId, Id changeId,
                             Tag tag) {
    if (changeId != null) {
      throw new UnsupportedOperationException(
          "In this plugin implementation tag is supported only on versionId");
    }
    copyElements(context, getSpaceName(context, Space.PRIVATE), itemId, versionId, tag.getName());
  }

  public CollaborationMergeChange resetItemVersionHistory(SessionContext context, Id itemId,
                                                          Id versionId, String changeRef) {
    ElementRepository elementRepository = getElementRepository(context);
    ElementEntityContext elementContext =
        new ElementEntityContext(getSpaceName(context, Space.PRIVATE), itemId, versionId);

    CollaborationMergeChange resetChange = new CollaborationMergeChange();

    Collection<ElementEntity> versionElements = elementRepository.list(context, elementContext);
    versionElements.stream()
        .map(elementEntity ->
            convertElementEntityToElementChange(elementEntity, elementContext, Action.DELETE))
        .forEach(resetChange.getChangedElements()::add);

    elementContext.setChangeRef(changeRef);
    Collection<ElementEntity> changeRefElements = elementRepository.list(context, elementContext);
    changeRefElements.stream()
        .map(elementEntity ->
            convertElementEntityToElementChange(elementEntity, elementContext, Action.CREATE))
        .forEach(resetChange.getChangedElements()::add);

    return resetChange; // TODO: 4/19/2017 version change...
  }

  private void copyElements(SessionContext context, String space, Id itemId, Id sourceVersionId,
                            String targetTag) {
    ElementRepository elementRepository = getElementRepository(context);
    ElementEntityContext elementContext = new ElementEntityContext(space, itemId, sourceVersionId);

    Collection<ElementEntity> versionElements = elementRepository.list(context, elementContext);

    elementContext.setChangeRef(targetTag);
    versionElements
        .forEach(elementEntity -> elementRepository.create(context, elementContext, elementEntity));
  }

  private CollaborationElementChange convertElementEntityToElementChange(
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
