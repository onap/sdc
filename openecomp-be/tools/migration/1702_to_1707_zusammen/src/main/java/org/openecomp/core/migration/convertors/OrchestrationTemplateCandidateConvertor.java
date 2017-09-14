package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.MigrationMain;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.StructureElement;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateDataEntity;

import java.util.List;

public class OrchestrationTemplateCandidateConvertor {




  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);

  public static CollaborationElement[] convertOrchestrationTemplateCandidateToElement(
      OrchestrationTemplateCandidateDataEntity entity) {


    CollaborationElement[] orchestrationTemplateCandidateElements = new CollaborationElement[2];
    List<String> orchestrationTemplateCandidateNamespace =
        getOrchestrationTemplateCandidateNamespace();



    orchestrationTemplateCandidateElements[0] = ElementHandler.getElementEntity(
        entity.getId(), entity.getVersion().toString(), StructureElement.OrchestrationTemplateCandidate.name(),
        orchestrationTemplateCandidateNamespace,
        ElementHandler.getStructuralElementInfo(StructureElement.OrchestrationTemplateCandidate.name()),
        null,
        null,
        entity.getFilesDataStructure().getBytes());

    orchestrationTemplateCandidateNamespace.add(StructureElement.OrchestrationTemplateCandidate.name());
    orchestrationTemplateCandidateElements[1] = ElementHandler.getElementEntity(
        entity.getId(), entity.getVersion().toString(), StructureElement.OrchestrationTemplateCandidateContent.name(),
        orchestrationTemplateCandidateNamespace,
        ElementHandler.getStructuralElementInfo(StructureElement.OrchestrationTemplateCandidateContent.name()),
        null,
        null,
        entity.getContentData().array());



    return orchestrationTemplateCandidateElements;
  }


  private static List<String> getOrchestrationTemplateCandidateNamespace() {
    return ElementHandler.getElementPath();
  }

  public static ElementEntityContext convertComponentToElementContext(
      ComponentEntity componentEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(componentEntity.getVspId(), componentEntity.getVersion().toString()));
  }




}
