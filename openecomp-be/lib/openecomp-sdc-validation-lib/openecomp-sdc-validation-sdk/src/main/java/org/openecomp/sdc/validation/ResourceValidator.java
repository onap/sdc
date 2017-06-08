package org.openecomp.sdc.validation;

import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.heat.datatypes.model.Resource;

import java.util.Map;

/**
 * Created by TALIO on 2/23/2017.
 */
public interface ResourceValidator {

  void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                GlobalValidationContext globalContext, ValidationContext validationContext);
}
