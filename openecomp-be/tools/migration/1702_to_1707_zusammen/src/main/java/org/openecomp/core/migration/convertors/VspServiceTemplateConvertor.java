package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.core.model.types.ServiceTemplate;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.model.impl.zusammen.ElementType;
import org.openecomp.sdc.model.impl.zusammen.StructureElement;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author katyr
 * @since April 23, 2017
 */

public class VspServiceTemplateConvertor 
{
    private static Set<String> serviceTemplatesLoaded = new HashSet<>();
 


  public static CollaborationElement[] convertServiceTemplateToElement(ServiceTemplate serviceTemplate) {

    CollaborationElement[] serviceTemplateElements;
    List<String> serviceTemplateNamespace = getServiceTemplateNamespace();

    int index = 0;
    String serviceTemplatesEntityId = StructureElement.Templates.name();
    String uniqueId = serviceTemplate.getVspId()+"_"+serviceTemplate.getVersion().toString();
    if (serviceTemplatesLoaded.contains(uniqueId)) {
      serviceTemplateElements = new CollaborationElement[1];
    } else {
      serviceTemplatesLoaded.add(uniqueId);
      String vspServiceModelEntityId = StructureElement.ServiceModel.name();

      serviceTemplateElements = new CollaborationElement[3];
      serviceTemplateElements[index] = ElementHandler.getElementEntity(
          serviceTemplate.getVspId(),
          serviceTemplate.getVersion().toString(),
          vspServiceModelEntityId,
          serviceTemplateNamespace,
          ElementHandler.getServiceModelElementInfo(vspServiceModelEntityId,serviceTemplate),
          null,
          null,
          null);
     index++;
      serviceTemplateNamespace.add(vspServiceModelEntityId);
      serviceTemplateElements[index] = ElementHandler.getElementEntity(
          serviceTemplate.getVspId(), serviceTemplate.getVersion().toString(), serviceTemplatesEntityId,
          serviceTemplateNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Templates.name()),
          null,
          null,
          null);
      index++;
    }

    serviceTemplateNamespace.add(serviceTemplatesEntityId);
    String elementId = serviceTemplate.getVspId()+"_"+serviceTemplate.getVersion().toString()
        +"_"+serviceTemplate.getName();
    serviceTemplateElements[index] = ElementHandler.getElementEntity(
        serviceTemplate.getVspId(), serviceTemplate.getVersion().toString(), elementId,
        serviceTemplateNamespace,
        getServiceTemplateInfo(serviceTemplate),
        null,
        null,
        FileUtils.toByteArray(serviceTemplate.getContent()));

    return serviceTemplateElements;
  }

  private static Info getServiceTemplateInfo(ServiceTemplate serviceTemplateEntity) {
    Info info = new Info();
    info.setName(serviceTemplateEntity.getName());
    info.addProperty("type", ElementType.Servicetemplate.name());
    info.addProperty("base", serviceTemplateEntity.getBaseName());



    return info;
  }

  private static List<String> getServiceTemplateNamespace() {
    return ElementHandler.getElementPath();
  }

  public static ElementEntityContext convertServiceTemplateToElementContext(ServiceTemplate
                                                                                serviceTemplateEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(serviceTemplateEntity.getVspId(), serviceTemplateEntity.getVersion().toString()));
  }

}
