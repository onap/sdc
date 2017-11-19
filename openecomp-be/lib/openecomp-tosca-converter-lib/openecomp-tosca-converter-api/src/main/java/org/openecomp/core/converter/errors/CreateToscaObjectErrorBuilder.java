package org.openecomp.core.converter.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public class CreateToscaObjectErrorBuilder extends BaseErrorBuilder {
  private static final String CANT_CREATE_OBJECT_FROM_CLASS_MSG =
      "Can't create %s from %s. Reason - %s";
  private static final String IMPORT_TOSCA = "IMPORT_TOSCA";

  public CreateToscaObjectErrorBuilder(String className,
                                       String objectId,
                                       String reason) {
    getErrorCodeBuilder()
        .withId(IMPORT_TOSCA)
        .withCategory(ErrorCategory.APPLICATION)
        .withMessage(String.format(CANT_CREATE_OBJECT_FROM_CLASS_MSG, className, objectId, reason));
  }
}
