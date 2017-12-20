package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.base.ResourceBaseValidator;
import org.openecomp.sdc.validation.type.ConfigConstants;
import org.openecomp.sdc.validation.type.NamingConventionValidationContext;

import java.util.Map;

/**
 * Created by TALIO on 2/15/2017.
 */
public class NamingConventionGuideLineValidator extends ResourceBaseValidator {

  @Override
  public void init(Map<String, Object> properties){
    super.init((Map<String, Object>) properties.get(ConfigConstants.Resource_Base_Validator));
  }

  @Override
  public ValidationContext createValidationContext(String fileName, String envFileName,
                                                      HeatOrchestrationTemplate
                                                          heatOrchestrationTemplate,
                                                      GlobalValidationContext globalContext){
    return new NamingConventionValidationContext(heatOrchestrationTemplate, envFileName);
  }
}
