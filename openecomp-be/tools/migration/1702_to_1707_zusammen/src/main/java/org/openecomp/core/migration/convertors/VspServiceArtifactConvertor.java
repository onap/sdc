package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.model.impl.zusammen.StructureElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author katyr
 * @since April 23, 2017
 */

public class VspServiceArtifactConvertor 
{
    private static Set<String> serviceArtifactsLoaded = new HashSet<>();
 


  public static CollaborationElement[] convertServiceArtifactToElement(ServiceArtifact serviceArtifact) {

    CollaborationElement[] serviceArtifactElements;
    List<String> serviceArtifactNamespace = getServiceArtifactNamespace();

    int index = 0;
    String serviceArtifactsEntityId = StructureElement.Artifacts.name();
    String uniqueId = serviceArtifact.getVspId()+"_"+serviceArtifact.getVersion().toString();
    if (serviceArtifactsLoaded.contains(uniqueId)) {
      serviceArtifactElements = new CollaborationElement[1];
    } else {
      serviceArtifactsLoaded.add(uniqueId);
      serviceArtifactElements = new CollaborationElement[2];
      serviceArtifactElements[index] = ElementHandler.getElementEntity(
          serviceArtifact.getVspId(), serviceArtifact.getVersion().toString(), serviceArtifactsEntityId,
          serviceArtifactNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Artifacts.name()),
          null,
          null,
          null);
      index++;
    }

    serviceArtifactNamespace.add(serviceArtifactsEntityId);
    String elementId = serviceArtifact.getVspId()+"_"+serviceArtifact.getVersion().toString()
        +"_"+serviceArtifact.getName();
    serviceArtifactElements[index] = ElementHandler.getElementEntity(
        serviceArtifact.getVspId(), serviceArtifact.getVersion().toString(), elementId,
        serviceArtifactNamespace,
        getServiceArtifactInfo(serviceArtifact),
        null,
        null,
        FileUtils.toByteArray(serviceArtifact.getContent()));

    return serviceArtifactElements;
  }

  private static Info getServiceArtifactInfo(ServiceArtifact serviceArtifactEntity) {
    Info info = new Info();
    info.setName(serviceArtifactEntity.getName());
    info.addProperty("type", org.openecomp.sdc.model.impl.zusammen.ElementType.Artifact.name());

    return info;
  }

  private static List<String> getServiceArtifactNamespace() {
    return ElementHandler.getElementPath(StructureElement.ServiceModel.name());
  }

  public static ElementEntityContext convertServiceArtifactToElementContext(ServiceArtifact
                                                                                serviceArtifactEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(serviceArtifactEntity.getVspId(), serviceArtifactEntity.getVersion().toString()));
  }

}
