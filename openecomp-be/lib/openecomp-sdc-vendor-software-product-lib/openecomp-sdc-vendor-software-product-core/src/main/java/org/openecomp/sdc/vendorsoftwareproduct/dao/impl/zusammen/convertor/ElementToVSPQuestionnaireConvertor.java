package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;

public class ElementToVSPQuestionnaireConvertor  extends ElementConvertor {
  @Override
  public VspQuestionnaireEntity convert( Element element) {
    if(element == null) return null;
    VspQuestionnaireEntity entity = new VspQuestionnaireEntity();
    entity.setQuestionnaireData(new String(FileUtils.toByteArray(element.getData())));
    return entity;
  }

}
