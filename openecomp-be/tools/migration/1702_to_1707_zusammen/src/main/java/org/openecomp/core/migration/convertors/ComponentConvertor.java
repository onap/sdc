package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.MigrationMain;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ElementPropertyName;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.StructureElement;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author katyr
 * @since April 23, 2017
 */

public class ComponentConvertor {

  private static Set<String> componentsLoaded = new HashSet<>();
  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);

  public static CollaborationElement[] convertComponentToElement(ComponentEntity componentEntity) {

    CollaborationElement[] componentElements;
    List<String> componentNamespace = getComponentNamespace();

    int index = 0;
    String componentsEntityId = StructureElement.Components.name();
    String uniqueId = componentEntity.getVspId()+"_"+componentEntity.getVersion().toString();
    if (componentsLoaded.contains(uniqueId)) {
//      printMessage(logger, "Components structural elements exist for component " +
//          componentEntity.getId());
      componentElements = new CollaborationElement[2];
    } else {
      componentsLoaded.add(uniqueId);
//      printMessage(logger, "Creating Components structural elements for component " +
//          componentEntity.getId());
      componentElements = new CollaborationElement[3];
      componentElements[index] = ElementHandler.getElementEntity(
          componentEntity.getVspId(), componentEntity.getVersion().toString(), componentsEntityId,
          componentNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Components.name()),
          null,
          null,
          null);
      index++;
    }

    componentNamespace.add(componentsEntityId);
    componentElements[index] = ElementHandler.getElementEntity(
        componentEntity.getVspId(), componentEntity.getVersion().toString(),
        componentEntity.getId(),
        componentNamespace,
        getComponentInfo(componentEntity),
        null,
        null,
        (componentEntity.getCompositionData() != null) ? componentEntity.getCompositionData().getBytes()
            : null);
    index++;

    componentNamespace.add(componentEntity.getId());
    componentElements[index] = ElementHandler.getElementEntity(
        componentEntity.getVspId(), componentEntity.getVersion().toString(),StructureElement.Questionnaire.name() + "_" + componentEntity.getId(),
        componentNamespace,
        ElementHandler.getStructuralElementInfo(StructureElement.Questionnaire.name()),
        null,
        null,
        (componentEntity.getQuestionnaireData() != null) ? componentEntity.getQuestionnaireData().getBytes()
            : null);

    return componentElements;
  }

  private static Info getComponentInfo(ComponentEntity componentEntity) {
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Component);
    info.addProperty(ElementPropertyName.compositionData.name(),
        componentEntity.getCompositionData());

    return info;
  }

  private static List<String> getComponentNamespace() {
    return ElementHandler.getElementPath();
  }

  public static ElementEntityContext convertComponentToElementContext(
      ComponentEntity componentEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(componentEntity.getVspId(), componentEntity.getVersion().toString()));
  }

}
