package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;

import java.nio.ByteBuffer;

public class ElementToProcessConvertor extends ElementConvertor<ProcessEntity> {

  @Override
  public ProcessEntity convert(Element element) {
    if (element == null) {
      return null;
    }
    ProcessEntity processEntity = new ProcessEntity();
    processEntity.setId(element.getElementId().getValue());
    processEntity.setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(element.getData())));
    mapInfoToProcessEntity(processEntity, element.getInfo());
    return processEntity;
  }

  @Override
  public ProcessEntity convert(ElementInfo elementInfo) {
    if (elementInfo == null) {
      return null;
    }
    ProcessEntity processEntity = new ProcessEntity();
    processEntity.setId(elementInfo.getId().getValue());
    mapInfoToProcessEntity(processEntity, elementInfo.getInfo());
    return processEntity;
  }


  public void mapInfoToProcessEntity(ProcessEntity processEntity, Info info) {
    processEntity.setName(info.getProperty(NAME));
    processEntity.setArtifactName(info.getProperty(ARTIFACT_NAME));
    processEntity.setDescription(info.getProperty(DESCRIPTION));
    processEntity.setType(info.getProperty
        (PROCESS_TYPE) != null ? ProcessType.valueOf(info.getProperty
        (PROCESS_TYPE)) : null);
  }


  public static final String NAME = "name";
  public static final String ARTIFACT_NAME = "artifactName";
  public static final String DESCRIPTION = "description";
  public static final String PROCESS_TYPE = "processType";
}
