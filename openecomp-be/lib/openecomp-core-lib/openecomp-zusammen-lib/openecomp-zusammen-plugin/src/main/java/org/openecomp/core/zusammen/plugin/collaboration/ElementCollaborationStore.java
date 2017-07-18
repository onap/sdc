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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    return elementRepository.get(context, elementEntityContext, new ElementEntity(elementId))
        .map(ElementEntity::getSubElementIds).orElse(new HashSet<>()).stream()
        .map(subElementId -> elementRepository
            .get(context, elementEntityContext, new ElementEntity(subElementId)).get())
        .filter(Objects::nonNull)
        .map(subElement -> ZusammenPluginUtil
            .getCollaborationElement(elementEntityContext, subElement))
        .collect(Collectors.toList());
  }

  public CollaborationElement getElement(SessionContext context, ElementContext elementContext,
                                         Id elementId) {
    ElementEntityContext elementEntityContext =
        new ElementEntityContext(ZusammenPluginUtil.getPrivateSpaceName(context), elementContext);
    return getElementRepository(context)
        .get(context, elementEntityContext, new ElementEntity(elementId))
        .map(elementEntity -> ZusammenPluginUtil
            .getCollaborationElement(elementEntityContext, elementEntity))
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

  public boolean checkHealth(SessionContext sessionContext){
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
