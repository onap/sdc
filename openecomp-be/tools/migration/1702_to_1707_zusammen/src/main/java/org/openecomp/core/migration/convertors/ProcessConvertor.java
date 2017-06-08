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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessConvertor {
  private static final String NAME = "name";
  private static final String ELEMENT_TYPE = "type";
  private static final String ARTIFACT_NAME = "artifactName";
  private static final String DESCRIPTION = "description";
  private static final String PROCESS_TYPE = "processType";
  private static Set<String> compProcessesLoaded = new HashSet<>();

  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);


  public static CollaborationElement[] convertProcessToElement(ProcessEntity processEntity) {
    CollaborationElement[] elements;
    boolean isGeneralComponentId = isGeneralComponentId(processEntity);
    List<String> processNamespace = getProcessNamespace(processEntity);
    int index = 0;
    String uniqueId = getUniqueId(processEntity, isGeneralComponentId);
    String processesEntityId = isGeneralComponentId ? StructureElement.Processes.name() :
        (StructureElement.Processes.name() + "_" +processEntity.getComponentId());
    if (compProcessesLoaded.contains(uniqueId)) {
      elements = new CollaborationElement[1];
    } else {
      compProcessesLoaded.add(uniqueId);
      elements = new CollaborationElement[2];
      elements[index++] = ElementHandler.getElementEntity(
          processEntity.getVspId(), processEntity.getVersion().toString(), processesEntityId,
          processNamespace,
          ElementHandler.getStructuralElementInfo(StructureElement.Processes.name()), null, null, null);
    }

    processNamespace.add(processesEntityId);
    elements[index] = ElementHandler.getElementEntity(
        processEntity.getVspId(), processEntity.getVersion().toString(), processEntity.getId(),
        processNamespace,
        getProcessInfo(processEntity), null, null, processEntity.getArtifact() ==
            null ? null : processEntity.getArtifact().array());

    return elements;
  }

  private static String getUniqueId(ProcessEntity processEntity, boolean isGeneralComponentId) {
    if (isGeneralComponentId) {
      return processEntity.getVspId() + "_" + processEntity.getVersion().toString();
    } else {
      return StructureElement.Processes.name() + "_" + processEntity
          .getComponentId();
    }
  }

  private static Info getProcessInfo(ProcessEntity processEntity) {
    Info info = new Info();
    info.setName(processEntity.getName());
    info.addProperty(NAME, processEntity.getName());
    info.addProperty(ELEMENT_TYPE, ElementType.Process);
    info.addProperty(ARTIFACT_NAME, processEntity.getArtifactName());
    info.addProperty(DESCRIPTION, processEntity.getDescription());
    info.addProperty(PROCESS_TYPE,
        processEntity.getType() != null ? processEntity.getType().name() : null);
    return info;
  }

  private static List<String> getProcessNamespace(ProcessEntity processEntity) {
    if (isGeneralComponentId(processEntity)) {
      return ElementHandler.getElementPath();
    } else {
      return ElementHandler
          .getElementPath(StructureElement.Components.name(), processEntity.getComponentId());
    }
  }

  private static boolean isGeneralComponentId(ProcessEntity processEntity) {
    return processEntity.getComponentId().equals("General");
  }

  public static ElementEntityContext convertProcessToElementContext(ProcessEntity processEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(processEntity.getVspId(), processEntity.getVersion().toString()));
  }
}
