package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.zusammen.plugin.ZusammenPluginConstants;
import org.openecomp.core.zusammen.plugin.ZusammenPluginUtil;
import org.openecomp.core.zusammen.plugin.dao.ElementRepository;
import org.openecomp.core.zusammen.plugin.dao.ElementRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getCollaborationElement;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getSpaceName;


public class ElementCollaborationStore {

  public Collection<CollaborationElement> listElements(SessionContext context,
                                                       ElementContext elementContext,
                                                       Id elementId) {
    ElementEntityContext elementEntityContext =
        new ElementEntityContext(ZusammenPluginUtil.getPrivateSpaceName(context), elementContext);

    if (elementId == null) {
      elementId = ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID;
    }

    ElementRepository elementRepository = getElementRepository(context);
    String elementIdValue = elementId.getValue();
    Collection<CollaborationElement> subElements = new ArrayList<>();

    Optional<ElementEntity> element =
        elementRepository.get(context, elementEntityContext, new ElementEntity(elementId));
    if (element.isPresent() && element.get().getSubElementIds() != null) {
      for (Id subElementId : element.get().getSubElementIds()) {
        ElementEntity subElement =
            elementRepository.get(context, elementEntityContext, new ElementEntity(subElementId))
                .orElseThrow(() -> new IllegalStateException(String.format(
                    "List sub elements error: item %s, version %s - " +
                        "element %s, which appears as sub element of element %s, does not exist",
                    elementContext.getItemId().getValue(), elementContext.getChangeRef() == null
                        ? elementContext.getVersionId().getValue()
                        : elementContext.getChangeRef(),
                    subElementId, elementIdValue)));
        subElements.add(getCollaborationElement(elementEntityContext, subElement));
      }
    }
    return subElements;
  }

  public CollaborationElement getElement(SessionContext context, ElementContext elementContext,
                                         Id elementId) {
    ElementEntityContext elementEntityContext =
        new ElementEntityContext(ZusammenPluginUtil.getPrivateSpaceName(context), elementContext);
    return getElementRepository(context)
        .get(context, elementEntityContext, new ElementEntity(elementId))
        .map(elementEntity -> getCollaborationElement(elementEntityContext, elementEntity))
        .orElse(null);
  }

  public void createElement(SessionContext context, CollaborationElement element) {
    getElementRepository(context)
        .create(context,
            new ElementEntityContext(getSpaceName(context, element.getSpace()),
                element.getItemId(), element.getVersionId()),
            ZusammenPluginUtil.getElementEntity(element));
  }

  public void updateElement(SessionContext context, CollaborationElement element) {
    getElementRepository(context)
        .update(context,
            new ElementEntityContext(getSpaceName(context, element.getSpace()),
                element.getItemId(), element.getVersionId()),
            ZusammenPluginUtil.getElementEntity(element));
  }

  public void deleteElement(SessionContext context, CollaborationElement element) {
    deleteElementHierarchy(getElementRepository(context),
        context,
        new ElementEntityContext(getSpaceName(context, element.getSpace()),
            element.getItemId(), element.getVersionId()),
        ZusammenPluginUtil.getElementEntity(element));
  }

  public boolean checkHealth(SessionContext sessionContext) {
    return getElementRepository(sessionContext).checkHealth(sessionContext);
  }

  private void deleteElementHierarchy(ElementRepository elementRepository, SessionContext context,
                                      ElementEntityContext elementEntityContext,
                                      ElementEntity elementEntity) {
    Optional<ElementEntity> retrieved =
        elementRepository.get(context, elementEntityContext, elementEntity);
    if (!retrieved.isPresent()) {
      return;
    }
    retrieved.get().getSubElementIds().stream()
        .map(ElementEntity::new)
        .forEach(subElementEntity -> deleteElementHierarchy(
            elementRepository, context, elementEntityContext, subElementEntity));

    // only for the first one the parentId will populated (so it'll be removed from its parent)
    elementRepository.delete(context, elementEntityContext, elementEntity);
  }

  protected ElementRepository getElementRepository(SessionContext context) {
    return ElementRepositoryFactory.getInstance().createInterface(context);
  }
}
