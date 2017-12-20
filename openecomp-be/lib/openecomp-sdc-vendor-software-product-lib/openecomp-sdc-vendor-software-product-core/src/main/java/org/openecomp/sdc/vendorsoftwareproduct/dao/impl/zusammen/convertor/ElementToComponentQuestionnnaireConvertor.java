package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;


/**
 * Created by ayalaben on 9/27/2017
 */
public class ElementToComponentQuestionnnaireConvertor extends ElementConvertor<ComponentEntity> {
  @Override
  public ComponentEntity convert( Element element) {
    ComponentEntity componentEntity = new ComponentEntity();

    componentEntity.setId(element.getElementId().getValue());
    componentEntity.setQuestionnaireData(new String(FileUtils.toByteArray(element.getData())));
    return componentEntity;
  }
}
