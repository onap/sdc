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
    copyElements(context, new ElementEntityContext(space, itemId, versionId), targetContext);
  }

  public CollaborationMergeChange resetItemVersionHistory(SessionContext context, Id itemId,
                                                          Id versionId, String changeRef) {
    ElementRepository elementRepository = getElementRepository(context);
    CollaborationMergeChange resetChange = new CollaborationMergeChange();

    ElementEntityContext versionContext =
        new ElementEntityContext(getSpaceName(context, Space.PRIVATE), itemId, versionId);
    Collection<ElementEntity> versionElements = elementRepository.list(context, versionContext);
    for (ElementEntity element : versionElements) {
      elementRepository.delete(context, versionContext, new ElementEntity(element.getId()));
      resetChange.getChangedElements()
          .add(convertElementEntityToElementChange(element, versionContext, Action.DELETE));
    }
    elementRepository.delete(context, versionContext, new ElementEntity(ROOT_ELEMENTS_PARENT_ID));

    ElementEntityContext changeRefContext =
        new ElementEntityContext(getSpaceName(context, Space.PRIVATE), itemId, versionId);
    changeRefContext.setChangeRef(changeRef);
    Collection<ElementEntity> changeRefElements = elementRepository.list(context, changeRefContext);
    for (ElementEntity element : changeRefElements) {
      elementRepository.create(context, versionContext, element);
      resetChange.getChangedElements()
          .add(convertElementEntityToElementChange(element, versionContext, Action.CREATE));
    }
    return resetChange; // TODO: 4/19/2017 version change...
  }

  private void copyElements(SessionContext context, ElementEntityContext sourceContext,
                            ElementEntityContext targetContext) {
    ElementRepository elementRepository = getElementRepository(context);

    Collection<ElementEntity> versionElements = elementRepository.list(context, sourceContext);
    versionElements
        .forEach(elementEntity -> elementRepository.create(context, targetContext, elementEntity));
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
