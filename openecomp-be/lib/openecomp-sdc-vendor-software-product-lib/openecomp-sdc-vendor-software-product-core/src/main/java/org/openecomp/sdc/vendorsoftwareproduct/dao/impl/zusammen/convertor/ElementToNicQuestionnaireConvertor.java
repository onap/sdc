package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;

public class ElementToNicQuestionnaireConvertor extends ElementConvertor <NicEntity>{

  @Override
  public NicEntity convert( Element element) {
    NicEntity nicEntity = new NicEntity();

    nicEntity.setId(element.getElementId().getValue());
    nicEntity.setQuestionnaireData( element.getData() == null
        ? null
        : new String(FileUtils.toByteArray(element.getData())));
    return nicEntity;
  }

}
