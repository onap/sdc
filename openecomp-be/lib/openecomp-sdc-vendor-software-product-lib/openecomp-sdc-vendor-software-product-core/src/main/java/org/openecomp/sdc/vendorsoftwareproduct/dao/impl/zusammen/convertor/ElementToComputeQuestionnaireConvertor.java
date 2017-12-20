package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;

public class ElementToComputeQuestionnaireConvertor extends ElementConvertor <ComputeEntity>{

  @Override
  public ComputeEntity convert( Element element) {
    ComputeEntity computeEntity = new ComputeEntity();

    computeEntity.setId(element.getElementId().getValue());
    computeEntity.setQuestionnaireData(new String(FileUtils.toByteArray(element.getData())));
    return computeEntity;
  }

}
