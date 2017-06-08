package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.MigrationMain;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.StructureElement;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MibConvertor {
  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);

  private static Set<String> compMibLoaded = new HashSet<>();


  public static CollaborationElement[] convertMibToElement(MibEntity mibEntity) {
    CollaborationElement[] elements;
    List<String> mibNamespace = getMibNamespace(mibEntity);

    int index = 0;
    String mibsEntityId = StructureElement.Mibs.name() + "_" + mibEntity.getComponentId();
    if (compMibLoaded.contains(mibsEntityId)) {
      elements = new CollaborationElement[1];
    } else {
      compMibLoaded.add(mibsEntityId);
      elements = new CollaborationElement[2];
      elements[index++] = ElementHandler.getElementEntity(
          mibEntity.getVspId(), mibEntity.getVersion().toString(), mibsEntityId, mibNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Mibs.name()), null, null, null);
    }

    mibNamespace.add(mibsEntityId);
    elements[index] = ElementHandler.getElementEntity(
        mibEntity.getVspId(), mibEntity.getVersion().toString(), mibEntity.getId(), mibNamespace,
        getMibInfo(mibEntity), null, null, mibEntity.getArtifact().array());

    return elements;
  }

  private static Info getMibInfo(MibEntity mibEntity) {
    Info info = new Info();
    info.setName(mibEntity.getType().toString());
    info.getProperties().put("name", mibEntity.getArtifactName());
    return info;
  }

  private static List<String> getMibNamespace(MibEntity mibEntity) {
    return ElementHandler.getElementPath(StructureElement.Components.name(), mibEntity
        .getComponentId());
  }

  public static ElementEntityContext convertMibToElementContext(MibEntity mibEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(mibEntity.getVspId(), mibEntity.getVersion().toString()));
  }
}
