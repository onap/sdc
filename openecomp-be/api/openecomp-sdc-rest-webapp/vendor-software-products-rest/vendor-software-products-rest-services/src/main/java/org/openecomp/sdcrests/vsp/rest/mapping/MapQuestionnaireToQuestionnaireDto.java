package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireDto;

import java.util.HashMap;

/**
 * Created by ayalaben on 9/26/2017
 */
public class MapQuestionnaireToQuestionnaireDto extends
    MappingBase<CompositionEntity, QuestionnaireDto> {

  @Override
  public void doMapping(CompositionEntity source, QuestionnaireDto target) {
    target.setId(source.getId());
    target.setQuestionareData(JsonUtil.json2Object(source.getQuestionnaireData(), HashMap.class));
  }
}
