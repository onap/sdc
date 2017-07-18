package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.MigrationMain;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.StructureElement;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MibConvertor {
  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);

  private static Set<String> compMibLoaded = new HashSet<>();


  public static CollaborationElement[] convertMibToElement(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    CollaborationElement[] elements;
    List<String> mibNamespace = getMibNamespace(componentMonitoringUploadEntity);

    int index = 0;
    String mibsEntityId =
        StructureElement.Mibs.name() + "_" + componentMonitoringUploadEntity.getComponentId();
    if (compMibLoaded.contains(mibsEntityId)) {
      elements = new CollaborationElement[1];
    } else {
      compMibLoaded.add(mibsEntityId);
      elements = new CollaborationElement[2];
      elements[index++] = ElementHandler.getElementEntity(
          componentMonitoringUploadEntity.getVspId(),
          componentMonitoringUploadEntity.getVersion().toString(), mibsEntityId, mibNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Mibs.name()), null, null, null);
    }

    mibNamespace.add(mibsEntityId);
    elements[index] = ElementHandler.getElementEntity(
        componentMonitoringUploadEntity.getVspId(),
        componentMonitoringUploadEntity.getVersion().toString(), componentMonitoringUploadEntity
            .getId(), mibNamespace,
        getMibInfo(componentMonitoringUploadEntity), null, null, componentMonitoringUploadEntity
            .getArtifact().array());

    return elements;
  }

  private static Info getMibInfo(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    Info info = new Info();
    info.setName(componentMonitoringUploadEntity.getType().toString());
    info.getProperties().put("name", componentMonitoringUploadEntity.getArtifactName());
    return info;
  }

  private static List<String> getMibNamespace(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    return ElementHandler
        .getElementPath(StructureElement.Components.name(), componentMonitoringUploadEntity
        .getComponentId());
  }

  public static ElementEntityContext convertMibToElementContext(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(componentMonitoringUploadEntity.getVspId(),
        componentMonitoringUploadEntity
            .getVersion().toString()));
  }
}
