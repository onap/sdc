package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;

import java.nio.ByteBuffer;

import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor.ARTIFACT_NAME;

public class ElementToComponentMonitoringUploadConvertor extends ElementConvertor<ComponentMonitoringUploadEntity> {

  @Override
  public ComponentMonitoringUploadEntity convert(Element element) {
    ComponentMonitoringUploadEntity mibEntity = new ComponentMonitoringUploadEntity();

    mibEntity.setId(element.getElementId().getValue());
    mibEntity.setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(element.getData())));
    mapInfoToComponentMonitoringUploadEntity(mibEntity, element.getInfo());
    return mibEntity;
  }

  @Override
  public ComponentMonitoringUploadEntity convert(ElementInfo elementInfo) {
    ComponentMonitoringUploadEntity mibEntity = new ComponentMonitoringUploadEntity();

    mibEntity.setId(elementInfo.getId().getValue());
    mapInfoToComponentMonitoringUploadEntity(mibEntity, elementInfo.getInfo());
    return mibEntity;
  }


  public void mapInfoToComponentMonitoringUploadEntity(ComponentMonitoringUploadEntity mibEntity,
                                                       Info info) {

    mibEntity.setArtifactName((String) info.getProperties().get(ARTIFACT_NAME));
    mibEntity.setType(MonitoringUploadType.valueOf(info.getName()));
  }


}
