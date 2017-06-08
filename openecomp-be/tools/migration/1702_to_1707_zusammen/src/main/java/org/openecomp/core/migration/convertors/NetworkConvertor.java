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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author katyr
 * @since April 23, 2017
 */

public class NetworkConvertor {

  private static Set<String> networksLoaded = new HashSet<>();
  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);



  public static CollaborationElement[] convertNetworkToElement(NetworkEntity networkEntity) {

    CollaborationElement[] networkElements;
    List<String> networkNamespace = getNetworkNamespace();

    int index = 0;
    String networksEntityId = StructureElement.Networks.name();
    String uniqueId = networkEntity.getVspId()+"_"+networkEntity.getVersion().toString();
    if (networksLoaded.contains(uniqueId)) {
//      printMessage(logger, "Networks structural elements exist for network " +
//          networkEntity.getId());
      networkElements = new CollaborationElement[1];
    } else {
//      printMessage(logger, "Creating Networks structural element for network " +
//          networkEntity.getId());
      networksLoaded.add(uniqueId);
      networkElements = new CollaborationElement[2];
      networkElements[index] = ElementHandler.getElementEntity(
          networkEntity.getVspId(), networkEntity.getVersion().toString(), networksEntityId,
          networkNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Networks.name()),
          null,
          null,
          null);
      index++;
    }

    networkNamespace.add(networksEntityId);

    networkElements[index] = ElementHandler.getElementEntity(
        networkEntity.getVspId(), networkEntity.getVersion().toString(), networkEntity.getId(),
        networkNamespace,
        getNetworkInfo(networkEntity),
        null,
        null,
        networkEntity.getCompositionData().getBytes());


    return networkElements;
  }

  private static Info getNetworkInfo(NetworkEntity networkEntity) {
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Network);
    info.addProperty(ElementPropertyName.compositionData.name(), networkEntity.getCompositionData());

    return info;
  }

  private static List<String> getNetworkNamespace() {
    return ElementHandler.getElementPath();
  }

  public static ElementEntityContext convertNetworkToElementContext(NetworkEntity networkEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(networkEntity.getVspId(), networkEntity.getVersion().toString()));
  }

}
