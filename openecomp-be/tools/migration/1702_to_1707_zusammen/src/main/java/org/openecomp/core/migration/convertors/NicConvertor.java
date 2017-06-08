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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NicConvertor {


  private static Set<String> compNicLoaded = new HashSet<>();
  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);



  public static CollaborationElement[] convertNicToElement(NicEntity nicEntity) {

    CollaborationElement[] nicElements;
    List<String> nicNamespace = getNicNamespace(nicEntity);

    int index = 0;
    String nicsEntityId = StructureElement.Nics.name() + "_" + nicEntity.getComponentId();
    if (compNicLoaded.contains(nicsEntityId)) {
//      printMessage(logger, "Nics structural element exists for nic " +
//          nicEntity.getId());
      nicElements = new CollaborationElement[2];
    } else {
//      printMessage(logger, "Creating Nics structural element for nic " +
//          nicEntity.getId());
      compNicLoaded.add(nicsEntityId);
      nicElements = new CollaborationElement[3];
      nicElements[index] = ElementHandler.getElementEntity(
          nicEntity.getVspId(), nicEntity.getVersion().toString(), nicsEntityId,
          nicNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Nics.name()),
          null,
          null,
          null);
      index++;
    }

    nicNamespace.add(nicsEntityId);
    nicElements[index] = ElementHandler.getElementEntity(
        nicEntity.getVspId(), nicEntity.getVersion().toString(), nicEntity.getId(),
        nicNamespace,
        getNicInfo(nicEntity),
        null,
        null,
        nicEntity.getCompositionData().getBytes());
    index++;

    nicNamespace.add(nicEntity.getId());
    nicElements[index] = ElementHandler.getElementEntity(
        nicEntity.getVspId(), nicEntity.getVersion().toString(),StructureElement.Questionnaire.name() + "_" + nicEntity.getId(),
        nicNamespace,
        ElementHandler.getStructuralElementInfo(StructureElement.Questionnaire.name()),
        null,
        null,
        (nicEntity.getQuestionnaireData() != null) ? nicEntity.getQuestionnaireData().getBytes()
            : null);
    return nicElements;
  }

  private static Info getNicInfo(NicEntity nicEntity) {
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Nic);
    info.addProperty(ElementPropertyName.compositionData.name(), nicEntity.getCompositionData());
    return info;
  }

  private static List<String> getNicNamespace(NicEntity nicEntity) {
    return ElementHandler.getElementPath(StructureElement.Components.name(), nicEntity
        .getComponentId());
  }

  public static ElementEntityContext convertNicToElementContext(NicEntity nicEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(nicEntity.getVspId(), nicEntity.getVersion().toString()));
  }


}
